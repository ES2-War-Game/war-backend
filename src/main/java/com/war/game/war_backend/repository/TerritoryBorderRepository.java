package com.war.game.war_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.war.game.war_backend.model.TerritoryBorder;

public interface TerritoryBorderRepository extends JpaRepository<TerritoryBorder, Long> {
  /**
   * Verifica se dois territórios são vizinhos checando a tabela territory_border. Retorna a
   * entidade TerritoryBorder se uma borda existir entre A e B.
   */
  @Query(
      "SELECT tb FROM TerritoryBorder tb WHERE "
          + "(tb.territoryA.id = :territoryAId AND tb.territoryB.id = :territoryBId) OR "
          + "(tb.territoryA.id = :territoryBId AND tb.territoryB.id = :territoryAId)")
  Optional<TerritoryBorder> findByTerritoryIds(Long territoryAId, Long territoryBId);

  @Query(
      "SELECT CASE WHEN COUNT(tb) > 0 THEN true ELSE false END FROM TerritoryBorder tb WHERE "
          + "(tb.territoryA.id = :territoryAId AND tb.territoryB.id = :territoryBId) OR "
          + "(tb.territoryA.id = :territoryBId AND tb.territoryB.id = :territoryAId)")
  boolean areTerritoryBordering(Long territoryAId, Long territoryBId);
}
