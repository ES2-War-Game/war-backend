package com.war.game.war_backend.services;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.war.game.war_backend.controller.dto.request.MovementRequestDto;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.Movement;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.repository.GameTerritoryRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.TerritoryBorderRepository;

@Service
public class MovementService {

    private static final Duration MOVEMENT_DURATION = Duration.ofSeconds(5);
    private static final String MOVEMENT_PREFIX = "movement:";

    @Autowired
    private RedisTemplate<String, Movement> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PlayerGameRepository playerGameRepository;

    @Autowired
    private GameTerritoryRepository gameTerritoryRepository;

    @Autowired
    private TerritoryBorderRepository territoryBorderRepository;

    @Transactional
    public Movement createMovement(MovementRequestDto request, Player currentPlayer) {
        // Validate player is in game and get player game instance
        PlayerGame playerGame = playerGameRepository.findByGame_IdAndPlayer_Id(request.getGameId(), currentPlayer.getId())
            .orElseThrow(() -> new IllegalStateException("Player not in game"));

        // Validate origin territory belongs to player's game
        GameTerritory originTerritory = gameTerritoryRepository
            .findByGame_IdAndTerritory_IdAndOwner(request.getGameId(), request.getOriginTerritoryId(), playerGame)
            .orElseThrow(() -> new IllegalStateException("Territory does not belong to player"));

        // Validate territories are adjacent
        boolean areAdjacent = territoryBorderRepository
            .areTerritoryBordering(request.getOriginTerritoryId(), request.getDestinationTerritoryId());
        if (!areAdjacent) {
            throw new IllegalStateException("Territories are not adjacent");
        }

        // Validate troop count
        if (originTerritory.getArmies() <= request.getTroops()) {
            throw new IllegalStateException("Not enough troops in territory");
        }

        // Create movement
        Movement movement = new Movement();
        movement.setId(UUID.randomUUID().toString());
        movement.setGameId(request.getGameId());
        movement.setPlayerId(currentPlayer.getId());
        movement.setOriginTerritoryId(request.getOriginTerritoryId());
        movement.setDestinationTerritoryId(request.getDestinationTerritoryId());
        movement.setTroops(request.getTroops());
        movement.setStartTime(Instant.now());
        movement.setEndTime(Instant.now().plus(MOVEMENT_DURATION));

        // Remove troops from origin
        originTerritory.setArmies(originTerritory.getArmies() - request.getTroops());
        gameTerritoryRepository.save(originTerritory);

        // Save to Redis with TTL
        String key = MOVEMENT_PREFIX + movement.getId();
        redisTemplate.opsForValue().set(key, movement, MOVEMENT_DURATION);

        // Notify all players in game
        messagingTemplate.convertAndSend("/topic/game/" + request.getGameId(), movement);

        return movement;
    }

    @Transactional
    public void cancelMovement(String movementId, Player currentPlayer) {
        String key = MOVEMENT_PREFIX + movementId;
        Movement movement = redisTemplate.opsForValue().get(key);

        if (movement == null) {
            throw new IllegalStateException("Movement not found");
        }

        if (!movement.getPlayerId().equals(currentPlayer.getId())) {
            throw new IllegalStateException("Movement does not belong to player");
        }

        // Return troops to origin territory
        GameTerritory originTerritory = gameTerritoryRepository
            .findByGame_IdAndTerritory_Id(movement.getGameId(), movement.getOriginTerritoryId())
            .orElseThrow(() -> new IllegalStateException("Origin territory not found"));

        originTerritory.setArmies(originTerritory.getArmies() + movement.getTroops());
        gameTerritoryRepository.save(originTerritory);

        // Remove from Redis
        redisTemplate.delete(key);

        // Notify cancellation
        messagingTemplate.convertAndSend("/topic/game/" + movement.getGameId(), 
            Map.of("type", "MOVEMENT_CANCELLED", "movementId", movementId));
    }

    @Transactional
    public void completeMovement(String movementId) {
        Movement movement = redisTemplate.opsForValue().get(MOVEMENT_PREFIX + movementId);
        if (movement == null) {
            return; // Movement might have been cancelled
        }

        GameTerritory destTerritory = gameTerritoryRepository
            .findByGame_IdAndTerritory_Id(movement.getGameId(), movement.getDestinationTerritoryId())
            .orElseThrow(() -> new IllegalStateException("Destination territory not found"));

        // Add troops to destination
        destTerritory.setArmies(destTerritory.getArmies() + movement.getTroops());
        gameTerritoryRepository.save(destTerritory);

        // Notify completion
        messagingTemplate.convertAndSend("/topic/game/" + movement.getGameId(),
            Map.of("type", "MOVEMENT_COMPLETED", "movement", movement));
    }
}
