package com.war.game.war_backend.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentTurnInfoDto {
  private Long gameId;
  private String gameName;
  private String gameStatus;
  private TurnPlayerInfo currentTurnPlayer;
  private Boolean isMyTurn;
  private Integer totalPlayers;
  private Integer activePlayers;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TurnPlayerInfo {
    private Long playerGameId;
    private String username;
    private String color;
    private Integer turnOrder;
    private Integer unallocatedArmies;
    private Boolean conqueredTerritoryThisTurn;
    private String imageUrl;
  }
}
