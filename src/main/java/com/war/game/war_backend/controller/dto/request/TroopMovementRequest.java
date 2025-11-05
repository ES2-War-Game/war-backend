package com.war.game.war_backend.controller.dto.request;

import lombok.Data;

@Data
public class TroopMovementRequest {
  private Long sourceTerritory;
  private Long targetTerritory;
  private Integer numberOfTroops;
  private Long gameId;
}
