package com.war.game.war_backend.controller.dto.response;

import lombok.Data;

@Data
public class GameResultResponse {
    private Long winningPlayerId;
    private String winningPlayerName;
    private String winningPlayerColor;
    private String winningPlayerImageUrl;
    private String winningCondition; // Ex: "OBJECTIVE_COMPLETED", "ELIMINATION_COMPLETE"
    private String objectiveDescription; // A descrição do objetivo se for o caso
}
