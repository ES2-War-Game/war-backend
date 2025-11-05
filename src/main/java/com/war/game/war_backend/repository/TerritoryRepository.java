package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.Territory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface TerritoryRepository extends JpaRepository<Territory, Long> {
    Optional<Territory> findByName(String name);

    long countByContinent(String continent);

    @NonNull
    List<Territory> findAll();
}
