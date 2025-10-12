package com.war.game.war_backend.repository;

import com.war.game.war_backend.model.TerritoryBorder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface TerritoryBorderRepository extends JpaRepository<TerritoryBorder, Long> {
    /**
     * Verifica se dois territórios são vizinhos checando a tabela territory_border.
     * Retorna a entidade TerritoryBorder se uma borda existir entre A e B.
     */
    @Query("SELECT tb FROM TerritoryBorder tb WHERE " +
           "(tb.territoryA.id = :territoryAId AND tb.territoryB.id = :territoryBId) OR " +
           "(tb.territoryA.id = :territoryBId AND tb.territoryB.id = :territoryAId)")
    Optional<TerritoryBorder> findByTerritoryIds(Long territoryAId, Long territoryBId);
}
