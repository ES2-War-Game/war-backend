package com.war.game.war_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;

@Repository
public interface PlayerGameRepository extends JpaRepository<PlayerGame, Long> {
    Optional<PlayerGame> findByGameAndPlayer(Game game, Player player);
    
    List<PlayerGame> findByGame(Game game);
    
    Optional<PlayerGame> findByGame_IdAndPlayer_Id(Long gameId, Long playerId);
    
    List<PlayerGame> findByPlayerAndGame_Status(Player player, String status);
    
    List<PlayerGame> findByPlayerAndStillInGame(Player player, Boolean stillInGame);
}
