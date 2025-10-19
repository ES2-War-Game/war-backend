package com.war.game.war_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.war.game.war_backend.controller.dto.request.TroopMovementRequest;
import com.war.game.war_backend.controller.dto.response.TroopMovementResponse;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.model.TroopMovement;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.GameTerritoryRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.TerritoryBorderRepository;
import com.war.game.war_backend.repository.TroopMovementRepository;
import com.war.game.war_backend.websocket.TroopMovementWebSocketService;

@Service
public class TroopMovementService {

    @Autowired
    private GameTerritoryRepository gameTerritoryRepository;

    @Autowired
    private TroopMovementRepository troopMovementRepository;

    @Autowired
    private PlayerGameRepository playerGameRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TerritoryBorderRepository territoryBorderRepository;

    @Autowired
    private TroopMovementWebSocketService webSocketService;

    @Transactional
    public TroopMovementResponse createTroopMovement(Long playerId, TroopMovementRequest request) {
        // Get the game and validate it's in progress
        var game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        if (!"IN_PROGRESS".equals(game.getStatus())) {
            throw new IllegalStateException("Game is not in progress");
        }
        
        // Get source and target territories
        GameTerritory sourceTerritory = gameTerritoryRepository.findById(request.getSourceTerritory())
                .orElseThrow(() -> new IllegalArgumentException("Source territory not found"));
        
        GameTerritory targetTerritory = gameTerritoryRepository.findById(request.getTargetTerritory())
                .orElseThrow(() -> new IllegalArgumentException("Target territory not found"));

        // Get the player's game and validate it's their turn
        PlayerGame playerGame = playerGameRepository.findByGame_IdAndPlayer_Id(request.getGameId(), playerId)
                .orElseThrow(() -> new RuntimeException("Player not found in game"));

        if (!playerGame.equals(game.getTurnPlayer())) {
            throw new RuntimeException("It's not your turn");
        }

        // Validate ownership and number of troops
        if (!sourceTerritory.getOwner().equals(playerGame)) {
            throw new RuntimeException("Player doesn't own the source territory");
        }

        if (sourceTerritory.getArmies() < request.getNumberOfTroops()) {
            throw new RuntimeException("Not enough troops in source territory");
        }

        // Validate if territories are bordering
        if (!territoryBorderRepository.areTerritoryBordering(
                sourceTerritory.getTerritory().getId(), 
                targetTerritory.getTerritory().getId())) {
            throw new RuntimeException("Territories are not bordering");
        }

        // Validate if troops are available for movement (not moved in this round)
        if (sourceTerritory.getAvailableArmies() < request.getNumberOfTroops()) {
            throw new RuntimeException("Not enough troops available for movement in this round");
        }

        // Validate if target territory is owned by the same player
        if (!targetTerritory.getOwner().equals(playerGame)) {
            throw new RuntimeException("Player doesn't own the target territory");
        }

        // Create troop movement
        TroopMovement movement = new TroopMovement();
        movement.setSourceTerritory(sourceTerritory);
        movement.setTargetTerritory(targetTerritory);
        movement.setNumberOfTroops(request.getNumberOfTroops());
        movement.setStatus("IN_PROGRESS");
        movement.setStartTime(LocalDateTime.now());
        movement.setEstimatedArrivalTime(LocalDateTime.now().plusMinutes(5)); // Example: 5 minutes travel time
        movement.setGame(sourceTerritory.getGame());
        movement.setPlayerGame(playerGame);

        // Check if there's already a movement in progress between these territories
        if (troopMovementRepository.existsByGameIdAndSourceTerritory_IdAndTargetTerritory_IdAndStatus(
                game.getId(), sourceTerritory.getId(), targetTerritory.getId(), "IN_PROGRESS")) {
            throw new IllegalStateException("There's already a troop movement in progress between these territories");
        }

        // Reduce troops from source territory and mark them as moved
        sourceTerritory.setArmies(sourceTerritory.getArmies() - request.getNumberOfTroops());
        sourceTerritory.markTroopsAsMoved(request.getNumberOfTroops());
        
        // Save source territory
        gameTerritoryRepository.save(sourceTerritory);

        // Save the movement
        movement = troopMovementRepository.save(movement);

        // Notify clients via WebSocket
        TroopMovementResponse response = convertToResponse(movement);
        webSocketService.notifyTroopMovementUpdate(request.getGameId(), response);

        return response;
    }

    public List<TroopMovementResponse> getTroopMovementsByGame(Long gameId) {
        return troopMovementRepository.findByGameId(gameId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void resetTroopMovementsForGame(Long gameId) {
        List<GameTerritory> territories = gameTerritoryRepository.findByGame_Id(gameId);
        for (GameTerritory territory : territories) {
            territory.resetMovedTroops();
        }
        gameTerritoryRepository.saveAll(territories);
    }

    @Scheduled(fixedDelay = 1000) // Executes every 1 second
    @Transactional
    public void processMovements() {
        LocalDateTime now = LocalDateTime.now();
        List<TroopMovement> completedMovements = troopMovementRepository
                .findByStatusAndEstimatedArrivalTimeLessThanEqual("IN_PROGRESS", now);

        for (TroopMovement movement : completedMovements) {
            // Update target territory troops
            GameTerritory targetTerritory = movement.getTargetTerritory();
            targetTerritory.setArmies(targetTerritory.getArmies() + movement.getNumberOfTroops());
            gameTerritoryRepository.save(targetTerritory);

            // Mark movement as completed
            movement.setStatus("COMPLETED");
            troopMovementRepository.save(movement);

            // Notify via WebSocket
            TroopMovementResponse response = convertToResponse(movement);
            webSocketService.notifyTroopMovementComplete(movement.getGame().getId(), response);
        }
    }

    private TroopMovementResponse convertToResponse(TroopMovement movement) {
        TroopMovementResponse response = new TroopMovementResponse();
        response.setId(movement.getId());
        response.setSourceTerritory(movement.getSourceTerritory().getId());
        response.setTargetTerritory(movement.getTargetTerritory().getId());
        response.setNumberOfTroops(movement.getNumberOfTroops());
        response.setStatus(movement.getStatus());
        response.setStartTime(movement.getStartTime());
        response.setEstimatedArrivalTime(movement.getEstimatedArrivalTime());
        return response;
    }
}
