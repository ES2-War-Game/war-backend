package com.war.game.war_backend.model;

import java.time.LocalDateTime;

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
@Table(name = "troop_movement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TroopMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_territory_id")
    private GameTerritory sourceTerritory;

    @ManyToOne
    @JoinColumn(name = "target_territory_id")
    private GameTerritory targetTerritory;

    @Column(nullable = false)
    private Integer numberOfTroops;

    @Column(nullable = false)
    private String status; // IN_PROGRESS, COMPLETED, CANCELLED

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime estimatedArrivalTime;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "player_game_id")
    private PlayerGame playerGame;
}
