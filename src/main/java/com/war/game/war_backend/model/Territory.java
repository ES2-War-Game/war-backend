package com.war.game.war_backend.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "territory")
@Getter
@Setter
@ToString(exclude = {"bordersA", "bordersB"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Territory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk_id")
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String name;

  @Column(nullable = false, length = 50)
  private String continent;

  @OneToMany(mappedBy = "territoryA")
  @JsonIgnore
  private List<TerritoryBorder> bordersA;

  @OneToMany(mappedBy = "territoryB")
  @JsonIgnore
  private List<TerritoryBorder> bordersB;

  @JsonIgnore
  public Set<TerritoryBorder> getAllBorders() {
    Set<TerritoryBorder> all = new HashSet<>();
    if (bordersA != null) all.addAll(bordersA);
    if (bordersB != null) all.addAll(bordersB);
    return all;
  }

  @JsonIgnore
  public Set<Territory> getNeighborTerritories() {
    Set<Territory> neighbors = new HashSet<>();
    for (TerritoryBorder border : getAllBorders()) {
      if (border.getTerritoryA() != null && !border.getTerritoryA().equals(this)) {
        neighbors.add(border.getTerritoryA());
      } else if (border.getTerritoryB() != null && !border.getTerritoryB().equals(this)) {
        neighbors.add(border.getTerritoryB());
      }
    }

    return neighbors;
  }
}
