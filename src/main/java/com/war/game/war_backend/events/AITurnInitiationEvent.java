package com.war.game.war_backend.events;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class AITurnInitiationEvent extends ApplicationEvent {

  private final Long gameId;
  private final String aiUsername;

  public AITurnInitiationEvent(Object source, Long gameId, String aiUsername) {
    super(source);
    this.gameId = gameId;
    this.aiUsername = aiUsername;
  }
}
