package com.war.game.war_backend.model.enums;

import java.util.List;

public class GameConstants {
    // Cores do War - valores CSS usados pelo frontend
    // Ordem: Blue, Red, Green, Yellow, Purple, Black
    public static final List<String> AVAILABLE_COLORS =
            List.of(
                    "blue", // Jogador azul
                    "red", // Jogador vermelho
                    "green", // Jogador verde
                    "#bfa640", // Jogador amarelo (cor específica)
                    "purple", // Jogador roxo
                    "black" // Jogador preto
                    );

    public static final int MAX_PLAYERS = 6;
    public static final int MIN_PLAYERS = 3;
}
