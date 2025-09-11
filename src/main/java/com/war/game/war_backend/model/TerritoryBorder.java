package com.war.game.war_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "territory_border")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerritoryBorder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk_id")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "territory_a_id", nullable = false)
  @JsonIgnore
  private Territory territoryA;

  @ManyToOne
  @JoinColumn(name = "territory_b_id", nullable = false)
  @JsonIgnore
  private Territory territoryB;

  @JsonProperty("territory_a_id")
  public Long getTerritoryAId() {
    return territoryA != null ? territoryA.getId() : null;
  }

  @JsonProperty("territory_b_id")
  public Long getTerritoryBId() {
    return territoryB != null ? territoryB.getId() : null;
  }
}
