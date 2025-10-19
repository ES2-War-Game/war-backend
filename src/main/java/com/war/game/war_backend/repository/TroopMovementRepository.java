package com.war.game.war_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.war.game.war_backend.model.TroopMovement;

public interface TroopMovementRepository extends JpaRepository<TroopMovement, Long> {
    List<TroopMovement> findByGameId(Long gameId);
    
    boolean existsByGameIdAndSourceTerritory_IdAndTargetTerritory_IdAndStatus(
        Long gameId, Long sourceTerritoryId, Long targetTerritoryId, String status);
        
    List<TroopMovement> findByStatusAndEstimatedArrivalTimeLessThanEqual(
        String status, LocalDateTime estimatedArrivalTime);
}
