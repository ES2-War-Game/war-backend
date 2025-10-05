package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.Territory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TerritoryRepository extends JpaRepository<Territory, Long> {
    Optional<Territory> findByName(String name);

    long countByContinent(String continent);
    List<Territory> findAll();

}
