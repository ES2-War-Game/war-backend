package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.Card;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {

}
