package com.war.game.war_backend.events;

import org.springframework.context.ApplicationEvent;

import com.war.game.war_backend.model.AITurnAction;

import lombok.Getter;

@Getter
public class AIActionIntentEvent extends ApplicationEvent {

  private final Long gameId;
  private final String aiUsername;
  private final AITurnAction action;

  public AIActionIntentEvent(Object source, Long gameId, String aiUsername, AITurnAction action) {
    super(source);
    this.gameId = gameId;
    this.aiUsername = aiUsername;
    this.action = action;
  }
}
