package com.war.game.war_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movement {
    private String id;
    private Long gameId;
    private Long playerId;
    private Long originTerritoryId;
    private Long destinationTerritoryId;
    private Integer troops;
    private Instant startTime;
    private Instant endTime;
}