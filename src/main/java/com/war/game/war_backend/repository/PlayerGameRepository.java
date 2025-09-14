package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.PlayerGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerGameRepository extends JpaRepository<PlayerGame, Long> {
}