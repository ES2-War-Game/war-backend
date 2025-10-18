package com.war.game.war_backend.controller.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TroopMovementResponse {
    private Long id;
    private Long sourceTerritory;
    private Long targetTerritory;
    private Integer numberOfTroops;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime estimatedArrivalTime;
}
