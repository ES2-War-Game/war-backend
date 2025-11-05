package com.war.game.war_backend.exceptions;

/**
 * Exception lançada quando uma ação é tentada em uma fase de jogo inválida. Por exemplo: tentar
 * atacar durante a fase de REINFORCEMENT.
 *
 * <p>Essa exceção deve resultar em HTTP 409 CONFLICT no controller.
 */
public class InvalidGamePhaseException extends RuntimeException {

  private final String currentPhase;
  private final String requiredPhase;

  public InvalidGamePhaseException(String message) {
    super(message);
    this.currentPhase = null;
    this.requiredPhase = null;
  }

  public InvalidGamePhaseException(String message, String currentPhase, String requiredPhase) {
    super(message);
    this.currentPhase = currentPhase;
    this.requiredPhase = requiredPhase;
  }

  public String getCurrentPhase() {
    return currentPhase;
  }

  public String getRequiredPhase() {
    return requiredPhase;
  }
}
