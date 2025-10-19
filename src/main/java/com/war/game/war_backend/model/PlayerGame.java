package com.war.game.war_backend.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

  @Column(name = "unallocated_armies", nullable = false)
  private Integer unallocatedArmies = 0;

  @Column(name = "conquered_territory_this_turn", nullable = false)
  private Boolean conqueredTerritoryThisTurn = false;

  @Column(name = "still_in_game", nullable = false)
  private Boolean stillInGame = true;
}
