package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.Territory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerritoryRepository extends JpaRepository<Territory, Long> {
}
