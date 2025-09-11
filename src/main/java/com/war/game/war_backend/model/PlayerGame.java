package com.war.game.war_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "player_game")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerGame {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk_id")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "player_id", referencedColumnName = "pk_id")
  private Player player;

  @ManyToOne
  @JoinColumn(name = "game_id", referencedColumnName = "pk_id")
  private Game game;

  @Column(nullable = true, length = 20)
  private String color;

  @Column(name = "is_ready", nullable = false)
  private Boolean isReady = false;

  @Column(name = "turn_order", nullable = true)
  private Integer turnOrder;

  @Column(name = "is_owner", nullable = false)
  private Boolean isOwner = false;

  @ManyToOne
  @JoinColumn(name = "objective_id", referencedColumnName = "pk_id", nullable = true)
  private Objective objective;

  @OneToMany(mappedBy = "playerGame")
  private Set<PlayerCard> playerCards;

  @OneToMany(mappedBy = "owner")
  private Set<GameTerritory> ownedTerritories;
}