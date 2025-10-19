package com.war.game.war_backend.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "game")
@Getter
@Setter
@ToString(exclude = {"turnPlayer", "winner"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Game {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk_id")
  private Long id;

  @Column(nullable = false, length = 50)
  private String status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @OneToOne
  @JoinColumn(name = "turn_player_id", referencedColumnName = "pk_id")
  private PlayerGame turnPlayer;

  @OneToOne
  @JoinColumn(name = "winner_id", referencedColumnName = "pk_id")
  private PlayerGame winner;

  @Column(name = "card_set_exchange_count", nullable = false)
  private Integer cardSetExchangeCount = 0;

  // Relacionamento com PlayerGame
  @OneToMany(mappedBy = "game")
  private Set<PlayerGame> playerGames;

  // Relacionamento com GameTerritory
  @OneToMany(mappedBy = "game")
  private Set<GameTerritory> gameTerritories;

  public List<Player> getPlayers() {
    return this.playerGames.stream()
        .map(PlayerGame::getPlayer)
        .collect(Collectors.toList());
  }


}
