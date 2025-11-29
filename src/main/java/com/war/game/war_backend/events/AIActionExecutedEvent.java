package com.war.game.war_backend.events;

import org.springframework.context.ApplicationEvent;

import com.war.game.war_backend.model.AITurnAction.ActionType;

import lombok.Getter;

@Getter
public class AIActionExecutedEvent extends ApplicationEvent {

  private final Long gameId;
  private final String aiUsername;
  private final ActionType completedActionType;
  private final boolean turnIsFinished;

  public AIActionExecutedEvent(
      Object source,
      Long gameId,
      String aiUsername,
      ActionType completedActionType,
      boolean turnIsFinished) {
    super(source);
    this.gameId = gameId;
    this.aiUsername = aiUsername;
    this.completedActionType = completedActionType;
    this.turnIsFinished = turnIsFinished;
  }
}
