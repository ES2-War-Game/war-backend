package com.war.game.war_backend.events;

import org.springframework.context.ApplicationEvent;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.PlayerGame;

public class GameOverEvent extends ApplicationEvent {

  private final Game game;
  private final PlayerGame winner;
  private final String condition;
  private final String objectiveDescription;

  /**
   * @param source O objeto que disparou o evento.
   * @param game A entidade Game a ser finalizada.
   * @param winner O PlayerGame vencedor.
   * @param condition A condição de vitória (ex: "OBJECTIVE_COMPLETED").
   * @param objectiveDescription A descrição do objetivo cumprido.
   */
  public GameOverEvent(
      Object source, Game game, PlayerGame winner, String condition, String objectiveDescription) {
    super(source);
    this.game = game;
    this.winner = winner;
    this.condition = condition;
    this.objectiveDescription = objectiveDescription;
  }

  public Game getGame() {
    return game;
  }

  public PlayerGame getWinner() {
    return winner;
  }

  public String getCondition() {
    return condition;
  }

  public String getObjectiveDescription() {
    return objectiveDescription;
  }
}
