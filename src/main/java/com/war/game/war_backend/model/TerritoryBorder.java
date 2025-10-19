package com.war.game.war_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "territory_border")
@Getter
@Setter
@ToString(exclude = {"territoryA", "territoryB"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
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
