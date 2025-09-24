package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.Territory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TerritoryRepository extends JpaRepository<Territory, Long> {
    Optional<Territory> findByName(String name);
}
