package com.war.game.war_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.war.game.war_backend.model.Territory;

@Repository
public interface TerritoryRepository extends JpaRepository<Territory, Long> {
  Optional<Territory> findByName(String name);

  long countByContinent(String continent);

  @NonNull
  List<Territory> findAll();
}
