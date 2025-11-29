package com.war.game.war_backend.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.war.game.war_backend.events.GameOverEvent;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.Objective;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.model.enums.GameStatus;

@Service
public class WinConditionService {

  // Simulação do mapeamento Continente -> Total de Territórios
  private static final Map<String, Long> CONTINENT_TERRITORY_COUNT =
      Map.of(
          "América do Sul", 4L,
          "América do Norte", 9L,
          "Europa", 7L,
          "África", 6L,
          "Ásia", 12L,
          "Oceania", 4L);

  private final ApplicationEventPublisher eventPublisher;

  public WinConditionService(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public boolean checkWinConditions(Game game, PlayerGame actingPlayerGame) {

    // Verificar Condição Padrão: Sobrevivência
    if (checkEliminationWin(game)) {
      return true;
    }

    // Verificar o Objetivo Secreto
    return checkObjectiveCompletion(game, actingPlayerGame);
  }

  // Lógica de Verificação de Sobrevivência
  private boolean checkEliminationWin(Game game) {
    long activePlayers = game.getPlayerGames().stream().filter(PlayerGame::getStillInGame).count();

    if (activePlayers == 1) {
      PlayerGame winner =
          game.getPlayerGames().stream().filter(PlayerGame::getStillInGame).findFirst().get();

      game.setStatus(GameStatus.FINISHED.name());
      game.setWinner(winner);

      eventPublisher.publishEvent(
          new GameOverEvent(
              this, game, winner, "ELIMINATION_COMPLETE", "Último jogador restante."));

      return true;
    }
    return false;
  }

  // Lógica de Verificação do Objetivo Secreto (Refatorada para disparar o Evento)
  public boolean checkObjectiveCompletion(Game game, PlayerGame playerGame) {
    Objective objective = playerGame.getObjective();

    if (objective == null) return false;

    boolean completed = false;
    String objectiveDescription = objective.getDescription();

    switch (objective.getType()) {
      case "CONQUER_CONTINENT":
        System.out.println("Checando conquista de continente...");
        completed = checkConquerContinent(playerGame, objective);
        System.out.println("resultado: " + completed);
        break;
      case "CONQUER_TERRITORIES":
        System.out.println("Checando conquista de território...");
        completed = checkConquerTerritories(playerGame, objective);
        System.out.println("resultado: " + completed);
        break;
      case "ELIMINATE_PLAYER":
        System.out.println("Checando Eliminação de player...");
        completed =
            checkEliminatePlayer(game, playerGame, objective) && playerGame.getStillInGame();
        System.out.println("resultado: " + completed);
        break;
    }

    if (completed) {
      eventPublisher.publishEvent(
          new GameOverEvent(this, game, playerGame, "OBJECTIVE_COMPLETED", objectiveDescription));

      return true;
    }
    return false;
  }

  // Verifica objetivos de conquista por quantidade de territórios
  private boolean checkConquerTerritories(PlayerGame playerGame, Objective objective) {
    String description = objective.getDescription();

    if (description.contains("24 territórios") || description.contains("26 Territórios")) {
      return playerGame.getOwnedTerritories().size() >= 24;
    }
    if (description.contains("18 territórios com pelo menos 2 exércitos")) {
      long qualifiedTerritories =
          playerGame.getOwnedTerritories().stream()
              .filter(t -> (t.getStaticArmies() + t.getMovedInArmies()) >= 2)
              .count();
      return qualifiedTerritories >= 18;
    }
    return false;
  }

  // Verifica objetivo de eliminação de um jogador específico
  private boolean checkEliminatePlayer(
      Game game, PlayerGame actingPlayerGame, Objective objective) {
    String description = objective.getDescription();
    String targetColor = extractTargetColorFromObjective(description);

    if (targetColor == null) return false;

    return game.getPlayerGames().stream()
        .filter(pg -> pg.getColor().equalsIgnoreCase(targetColor))
        .findFirst()
        .map(targetPlayerGame -> !targetPlayerGame.getStillInGame())
        .orElse(false);
  }

  private String extractTargetColorFromObjective(String description) {
    if (description == null || description.isEmpty()) {
      return null;
    }
    Map<String, String> colorMapping =
        Map.of(
            "verdes", "green",
            "azuis", "blue",
            "vermelhos", "red",
            "amarelos", "#bfa640",
            "pretos", "black",
            "roxos", "purple");
    String descriptionLower = description.toLowerCase();
    for (Map.Entry<String, String> entry : colorMapping.entrySet()) {
      if (descriptionLower.contains(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  // Verifica se o jogador conquistou os continentes necessários (COMPLEXO)
  private boolean checkConquerContinent(PlayerGame playerGame, Objective objective) {
    String description = objective.getDescription();
    List<String> requiredContinents = extractRequiredContinents(description);

    Map<String, Long> ownedTerritoriesPerContinent =
        playerGame.getOwnedTerritories().stream()
            .collect(
                Collectors.groupingBy(
                    gt -> gt.getTerritory().getContinent(), Collectors.counting()));

    int successfullyConquered = 0;

    for (String continent : requiredContinents) {
      if (ownedTerritoriesPerContinent.containsKey(continent)) {
        Long ownedCount = ownedTerritoriesPerContinent.get(continent);
        Long totalCount = CONTINENT_TERRITORY_COUNT.getOrDefault(continent, 0L);

        if (ownedCount.equals(totalCount) && totalCount > 0) {
          successfullyConquered++;
        }
      }
    }

    System.out.println("Continentes obrigatórios conquistados: " + successfullyConquered);

    if (description.contains("e mais um continente")) {
      boolean requiredAreConquered = successfullyConquered == requiredContinents.size();

      long totalContinentsControlled =
          ownedTerritoriesPerContinent.keySet().stream()
              .filter(
                  continent ->
                      ownedTerritoriesPerContinent
                              .get(continent)
                              .equals(CONTINENT_TERRITORY_COUNT.getOrDefault(continent, 0L))
                          && CONTINENT_TERRITORY_COUNT.getOrDefault(continent, 0L) > 0)
              .count();
      
      System.out.println("Continentes conquistados: " + totalContinentsControlled);

      return requiredAreConquered && totalContinentsControlled >= requiredContinents.size() + 1;
    }

    return successfullyConquered == requiredContinents.size();
  }

  // Auxiliar para extrair continentes da descrição
  private List<String> extractRequiredContinents(String description) {
    List<String> allContinents =
        List.of("América do Sul", "América do Norte", "Europa", "África", "Ásia", "Oceania");

    List<String> required =
        allContinents.stream().filter(description::contains).collect(Collectors.toList());

    return required;
  }
}
