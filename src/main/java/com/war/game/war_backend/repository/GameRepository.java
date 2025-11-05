package com.war.game.war_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.war.game.war_backend.model.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
  List<Game> findByStatus(String status);
}
