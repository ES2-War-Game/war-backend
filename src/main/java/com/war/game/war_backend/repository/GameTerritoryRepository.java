package com.war.game.war_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.PlayerGame;

@Repository
public interface GameTerritoryRepository extends JpaRepository<GameTerritory, Long> {

    List<GameTerritory> findByGameAndOwner(Game game, PlayerGame playerGame);
    Optional<GameTerritory> findByGameAndTerritoryId(Game game, Long territoryId);
    List<GameTerritory> findByGame(Game game);
    long countByOwner(PlayerGame owner);
    
    Optional<GameTerritory> findByGame_IdAndTerritory_Id(Long gameId, Long territoryId);
    Optional<GameTerritory> findByGame_IdAndTerritory_IdAndOwner_Player_Id(Long gameId, Long territoryId, Long playerId);
}
