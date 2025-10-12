package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.PlayerGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameTerritoryRepository extends JpaRepository<GameTerritory, Long> {

    List<GameTerritory> findByGameAndOwner(Game game, PlayerGame playerGame);
    Optional<GameTerritory> findByGameAndTerritoryId(Game game, Long territoryId);
    List<GameTerritory> findByGame(Game game);
    long countByOwner(PlayerGame owner); // Conta quantos GameTerritories pertencem a um PlayerGame específico (Para gameover).
}