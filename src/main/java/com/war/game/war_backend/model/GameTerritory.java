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
  private Game game;

  @ManyToOne
  @JoinColumn(name = "territory_id", referencedColumnName = "pk_id")
  private Territory territory;

  @ManyToOne
  @JoinColumn(name = "player_game_id", referencedColumnName = "pk_id", nullable = true)
  private PlayerGame owner;

  @Column(nullable = false)
  private Integer armies = 0;

  public void setArmies(Integer armies) {
    this.armies = armies;
  }

  public Integer getArmies() {
    return this.armies;
  }
}
