package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerGameRepository extends JpaRepository<PlayerGame, Long> {
    Optional<PlayerGame> findByGameAndPlayer(Game game, Player player);
    
    List<PlayerGame> findByGame(Game game);
}