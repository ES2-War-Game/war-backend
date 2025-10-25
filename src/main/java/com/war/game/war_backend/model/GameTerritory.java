package com.war.game.war_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

  @Column(name = "static_armies", nullable = false)
  private Integer staticArmies = 0;

  @Column(name = "moved_in_armies", nullable = false)
  private Integer movedInArmies = 0;

  @Column(name = "unallocated_armies", nullable = false)
  private Integer unallocatedArmies = 0;

  public Integer getArmies() {
    return this.staticArmies + this.movedInArmies;
  }

  public void consolidateArmies() {
    this.staticArmies = this.getArmies();
    this.movedInArmies = 0;
  }

  public void addUnallocatedArmies(Integer armies) {
    this.unallocatedArmies += armies;
  }

  public void allocateArmies(Integer armies) {
    if (armies > this.unallocatedArmies) {
        throw new IllegalArgumentException("Cannot allocate more armies than available");
    }
    this.staticArmies += armies;
    this.unallocatedArmies -= armies;
  }
}
