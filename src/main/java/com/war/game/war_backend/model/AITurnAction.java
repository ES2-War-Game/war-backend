package com.war.game.war_backend.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class AITurnAction {

  public enum ActionType {
    CARD_TRADE,
    REINFORCE_ALLOCATION, // Para alocação inicial ou por cartas
    ATTACK,
    FORTIFY,
    PASS_PHASE, // Sinal para transição de fase
    PASS_TURN // Sinal para passar o turno para o próximo jogador
  }

  private final ActionType type;
  private final String sourceTerritoryId; // Usado para ataque/fortificação
  private final String targetTerritoryId; // Usado para ataque/fortificação/alocação
  private final int numberOfArmies;
  private final List<Long> cardIds; // Usado para CARD_TRADE
  // ... outros campos necessários ...
}
