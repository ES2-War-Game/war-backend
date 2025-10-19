package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.PlayerCard;
import com.war.game.war_backend.model.PlayerGame;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerCardRepository extends JpaRepository<PlayerCard, Long> {

    List<PlayerCard> findByPlayerGame(PlayerGame playerGame);

    List<PlayerCard> findByPlayerGameAndIdIn(PlayerGame playerGame, List<Long> ids);

    long countByPlayerGame(PlayerGame playerGame);
}
