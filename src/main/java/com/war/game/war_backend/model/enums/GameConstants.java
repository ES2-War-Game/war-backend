package com.war.game.war_backend.model.enums;

import java.util.List;

public class GameConstants {
    // Cores padrão do War, em maiúsculas para padronização.
    public static final List<String> AVAILABLE_COLORS = List.of(
        "VERMELHO", "AZUL", "AMARELO", "VERDE", "PRETO", "ROSA"
    );
    
    public static final int MAX_PLAYERS = 6;
    public static final int MIN_PLAYERS = 3;
}