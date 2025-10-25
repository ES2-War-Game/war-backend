package com.war.game.war_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "game_territory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameTerritory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk_id")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "game_id", referencedColumnName = "pk_id")
  @JsonIgnore
  private Game game;

  @ManyToOne
  @JoinColumn(name = "territory_id", referencedColumnName = "pk_id")
  private Territory territory;

  @ManyToOne
  @JoinColumn(name = "player_game_id", referencedColumnName = "pk_id", nullable = true)
  @JsonIgnore
  private PlayerGame owner;

  @Column(nullable = false)
  private Integer armies = 0;

  @Column(name = "moved_armies", nullable = false)
  private Integer movedArmies = 0;

  @Column(name = "available_armies", nullable = false)
  private Integer availableArmies = 0;

  public void setArmies(Integer armies) {
    this.armies = armies;
    this.availableArmies = armies - this.movedArmies;
  }

  public Integer getArmies() {
    return this.armies;
  }

  public void updateAvailableArmies() {
    this.availableArmies = this.armies - this.movedArmies;
  }

  public void markTroopsAsMoved(Integer numberOfTroops) {
    this.movedArmies += numberOfTroops;
    this.updateAvailableArmies();
  }

  public void resetMovedTroops() {
    this.movedArmies = 0;
    this.updateAvailableArmies();
  }
}
