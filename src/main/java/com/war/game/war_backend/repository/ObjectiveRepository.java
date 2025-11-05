package com.war.game.war_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.war.game.war_backend.model.Objective;

public interface ObjectiveRepository extends JpaRepository<Objective, Long> {}
