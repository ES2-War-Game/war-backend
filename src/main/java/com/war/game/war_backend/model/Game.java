package com.war.game.war_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import com.war.game.war_backend.model.PlayerGame;

import java.util.Set;

@Entity
@Table(name = "game")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 50)
    private String status;

    @OneToOne
    @JoinColumn(name = "turn_player_id", referencedColumnName = "pk_id")
    private PlayerGame turnPlayer;

    @OneToOne
    @JoinColumn(name = "winner_id", referencedColumnName = "pk_id")
    private PlayerGame winner;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Relacionamento com PlayerGame
    @OneToMany(mappedBy = "game")
    private Set<PlayerGame> playerGames;
    
    // Relacionamento com GameTerritory
    @OneToMany(mappedBy = "game")
    private Set<GameTerritory> gameTerritories;
}