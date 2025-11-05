package com.war.game.war_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_card")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_game_id", referencedColumnName = "pk_id")
    private PlayerGame playerGame;

    @ManyToOne
    @JoinColumn(name = "card_id", referencedColumnName = "pk_id")
    private Card card;
}
