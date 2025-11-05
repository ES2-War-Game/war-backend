package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerGameRepository extends JpaRepository<PlayerGame, Long> {
    Optional<PlayerGame> findByGameAndPlayer(Game game, Player player);

    List<PlayerGame> findByGame(Game game);

    Optional<PlayerGame> findByGame_IdAndPlayer_Id(Long gameId, Long playerId);

    List<PlayerGame> findByPlayerAndGame_Status(Player player, String status);

    List<PlayerGame> findByPlayerAndStillInGame(Player player, Boolean stillInGame);

    // Query nativa para deletar PlayerGame diretamente do banco, ignorando cache do Hibernate
    @Modifying
    @Query(value = "DELETE FROM player_game WHERE pk_id = :playerGameId", nativeQuery = true)
    int deleteByIdNative(@Param("playerGameId") Long playerGameId);
}
