package com.war.game.war_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.war.game.war_backend.model.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {

  Optional<Player> findByUsername(String username);

  Optional<Player> findByEmail(String email);
}
