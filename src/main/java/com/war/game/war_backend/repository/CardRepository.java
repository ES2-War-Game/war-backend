package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    // Encontra uma carta aleatória que não está em uso
    @Query(value = "SELECT * FROM card c WHERE c.pk_id NOT IN (SELECT pc.card_id FROM player_card pc) ORDER BY RANDOM() LIMIT 1",
           nativeQuery = true)
    Optional<Card> findRandomUnownedCard();
}
