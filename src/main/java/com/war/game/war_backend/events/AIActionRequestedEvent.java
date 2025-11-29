package com.war.game.war_backend.events;

public class AIActionRequestedEvent {
  private final Long gameId;
  private final String username;

  public AIActionRequestedEvent(Long gameId, String username) {
    this.gameId = gameId;
    this.username = username;
  }

  public Long getGameId() {
    return gameId;
  }

  public String getUsername() {
    return username;
  }
}
