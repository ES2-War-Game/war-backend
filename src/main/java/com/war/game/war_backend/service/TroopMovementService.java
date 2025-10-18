package com.war.game.war_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.war.game.war_backend.repository.TroopMovementRepository;

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

    @Transactional
    public TroopMovementResponse createTroopMovement(Long playerId, TroopMovementRequest request) {
        // Get source and target territories
        GameTerritory sourceTerritory = gameTerritoryRepository.findById(request.getSourceTerritory())
                .orElseThrow(() -> new RuntimeException("Source territory not found"));
        
        GameTerritory targetTerritory = gameTerritoryRepository.findById(request.getTargetTerritory())
                .orElseThrow(() -> new RuntimeException("Target territory not found"));

        // Get the player's game
        PlayerGame playerGame = playerGameRepository.findByGame_IdAndPlayer_Id(request.getGameId(), playerId)
                .orElseThrow(() -> new RuntimeException("Player not found in game"));

        // Validate ownership and number of troops
        if (!sourceTerritory.getOwner().equals(playerGame)) {
            throw new RuntimeException("Player doesn't own the source territory");
        }

        if (sourceTerritory.getArmies() < request.getNumberOfTroops()) {
            throw new RuntimeException("Not enough troops in source territory");
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

        // Reduce troops from source territory
        sourceTerritory.setArmies(sourceTerritory.getArmies() - request.getNumberOfTroops());
        gameTerritoryRepository.save(sourceTerritory);

        // Save the movement
        movement = troopMovementRepository.save(movement);

        return convertToResponse(movement);
    }

    public List<TroopMovementResponse> getTroopMovementsByGame(Long gameId) {
        return troopMovementRepository.findByGameId(gameId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
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
