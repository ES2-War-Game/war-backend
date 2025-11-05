package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.Game;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByStatus(String status);

    @Query(
            "SELECT DISTINCT g FROM Game g "
                    + "LEFT JOIN FETCH g.playerGames pg "
                    + "LEFT JOIN FETCH pg.player "
                    + "WHERE g.id = :gameId")
    Optional<Game> findByIdWithPlayers(@Param("gameId") Long gameId);

    @Query(
            "SELECT DISTINCT g FROM Game g "
                    + "LEFT JOIN FETCH g.playerGames pg "
                    + "LEFT JOIN FETCH pg.player "
                    + "WHERE g.status = :status")
    List<Game> findByStatusWithPlayers(@Param("status") String status);
}
