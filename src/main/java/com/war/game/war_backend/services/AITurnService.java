package com.war.game.war_backend.services;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.war.game.war_backend.events.AIActionExecutedEvent;
import com.war.game.war_backend.events.AIActionIntentEvent;
import com.war.game.war_backend.events.AITurnInitiationEvent;
import com.war.game.war_backend.model.AITurnAction;
import com.war.game.war_backend.model.AITurnAction.ActionType;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerCard;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.model.enums.CardType;
import com.war.game.war_backend.model.enums.GameStatus;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.TerritoryBorderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AITurnService {

  private final ApplicationEventPublisher eventPublisher;
  private final GameRepository gameRepository;
  private final PlayerGameRepository playerGameRepository;
  private final TerritoryBorderRepository territoryBorderRepository;

  @Async
  public void startTurn(AITurnInitiationEvent event) {
    Long gameId = event.getGameId();
    String aiUsername = event.getAiUsername();

    System.out.println("IA - EVENTO RECEBIDO. INICIANDO TURNO: " + aiUsername);

    try {
      System.out.println("IA - INICIANDO TURNO: " + aiUsername);

      // Carregar o status inicial do jogo
      Game game =
          gameRepository
              .findById(gameId)
              .orElseThrow(() -> new RuntimeException("Partida não encontrada para IA."));

      String currentStatus = game.getStatus();

      // Decisão inicial baseada no status
      if (GameStatus.SETUP_ALLOCATION.name().equals(currentStatus)
          || GameStatus.REINFORCEMENT.name().equals(currentStatus)) {
        // Inicia a Fase de Cartas, que levará ao Reforço, Ataque, etc., através dos listeners
        executeCardTradePhase(gameId, aiUsername);
      } else {
        System.err.println(
            "IA - Chamada em fase inesperada: "
                + currentStatus
                + ". Publicando PASS_TURN para evitar travamento.");
        publishActionIntent(gameId, aiUsername, ActionType.PASS_TURN, null);
      }

    } catch (Exception e) {
      System.err.println("Erro Fatal no turno da IA (" + aiUsername + "): " + e.getMessage());
    }
  }

  @Transactional
  private void executeReinforcementPhase(Long gameId, String aiUsername) {
    // Carregar o Estado Inicial
    Game game =
        gameRepository
            .findById(gameId)
            .orElseThrow(() -> new RuntimeException("Partida não encontrada para IA."));

    final Player aiPlayer =
        game.getPlayers().stream()
            .filter(p -> p.getUsername().equals(aiUsername))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Jogador IA não encontrado."));

    PlayerGame initialAiPlayerGame =
        playerGameRepository
            .findByGameAndPlayer(game, aiPlayer)
            .orElseThrow(() -> new RuntimeException("PlayerGame da IA não encontrado."));

    // 1. Condição de Saída
    if (initialAiPlayerGame.getUnallocatedArmies() <= 0) {
      System.out.println("IA - Sem exércitos para alocar. Publicando intenção de PASSAR FASE.");

      // Se a alocação terminou, a IA publica a intenção de transicionar de fase
      // Este evento será pego pelo GameService, que chamará startNextTurn para ir ao ATTACK
      publishActionIntent(gameId, aiUsername, ActionType.PASS_PHASE, null);
      return;
    }

    // --- DECISÃO DE ALOCAÇÃO ---
    try {
      // Obter o Game Territories para a lógica de decisão
      Set<GameTerritory> aiTerritories =
          game.getGameTerritories().stream()
              .filter(gt -> gt.getOwner() != null && gt.getOwner().equals(initialAiPlayerGame))
              .collect(Collectors.toSet());

      // Estratégia de Decisão
      GameTerritory bestTarget = findMostVulnerableTerritory(aiTerritories);

      if (bestTarget == null) {
        bestTarget = aiTerritories.stream().findAny().orElse(null);
        if (bestTarget == null) {
          // Se não há territórios para alocar, passa a fase (embora o unallocatedArmies > 0)
          publishActionIntent(gameId, aiUsername, ActionType.PASS_PHASE, null);
          return;
        }
      }

      // Quantidade a Alocar (1 por vez)
      int troopsToAllocate = 1;

      // Cria e Publica a Intenção de Ação
      AITurnAction intent =
          AITurnAction.builder()
              .type(ActionType.REINFORCE_ALLOCATION)
              .targetTerritoryId(String.valueOf(bestTarget.getTerritory().getId()))
              .numberOfArmies(troopsToAllocate)
              .build();

      System.out.println(
          "IA - Publicando INTENÇÃO de alocar 1 tropa em: " + bestTarget.getTerritory().getName());
      publishActionIntent(gameId, aiUsername, ActionType.REINFORCE_ALLOCATION, intent);

    } catch (RuntimeException e) {
      System.err.println("IA falhou ao decidir a alocação de tropas: " + e.getMessage());
      // Em caso de erro, publica a intenção de passar a fase para não travar o jogo
      publishActionIntent(gameId, aiUsername, ActionType.PASS_PHASE, null);
    }
  }

  @Transactional
  private void executeAttackPhase(Long gameId, String aiUsername) {
    // Carregar Game e PlayerGame
    Game game =
        gameRepository
            .findById(gameId)
            .orElseThrow(() -> new RuntimeException("Partida não encontrada para IA."));

    Player aiPlayer =
        game.getPlayers().stream()
            .filter(p -> p.getUsername().equals(aiUsername))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Jogador IA não encontrado."));

    PlayerGame aiPlayerGame =
        playerGameRepository
            .findByGameAndPlayer(game, aiPlayer)
            .orElseThrow(() -> new RuntimeException("PlayerGame da IA não encontrado."));

    // --- DECISÃO ÚNICA DE ATAQUE ---
    AttackDecision decision = findBestAttack(game, aiPlayerGame);

    if (decision == null) {
      System.out.println(
          "IA - Não há mais ataques bons. Publicando intenção de PASSAR FASE (para Fortificação).");

      // Condição de Saída: Não há ataques ideais. Publica a intenção de ir para a próxima fase.
      publishActionIntent(gameId, aiUsername, ActionType.PASS_PHASE, null);
      return;
    }

    try {
      // Cria a intenção de Ação
      AITurnAction intent =
          AITurnAction.builder()
              .type(ActionType.ATTACK)
              .sourceTerritoryId(String.valueOf(decision.fromTerritoryId()))
              .targetTerritoryId(String.valueOf(decision.toTerritoryId()))
              .numberOfArmies(decision.numDice())
              .build();

      System.out.println(
          "IA - Publicando INTENÇÃO de ATACAR "
              + decision.toTerritoryId()
              + " de "
              + decision.fromTerritoryId());

      // Publica o Evento de Intenção de Ação
      publishActionIntent(gameId, aiUsername, ActionType.ATTACK, intent);

      // Retorna Imediatamente
    } catch (RuntimeException e) {
      System.err.println("IA falhou ao decidir o ataque: " + e.getMessage());
      // Em caso de erro, passa a fase
      publishActionIntent(gameId, aiUsername, ActionType.PASS_PHASE, null);
    }
  }

  @Transactional
  private void executeFortificationPhase(Long gameId, String aiUsername) {
    // Carregar o Estado do Jogo e Jogador
    Game game =
        gameRepository
            .findById(gameId)
            .orElseThrow(() -> new RuntimeException("Partida não encontrada para IA."));

    Player aiPlayer =
        game.getPlayers().stream()
            .filter(p -> p.getUsername().equals(aiUsername))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Jogador IA não encontrado."));

    PlayerGame aiPlayerGame =
        playerGameRepository
            .findByGameAndPlayer(game, aiPlayer)
            .orElseThrow(() -> new RuntimeException("PlayerGame da IA não encontrado."));

    // Obter todos os GameTerritorys da IA
    Set<GameTerritory> aiTerritories =
        game.getGameTerritories().stream()
            .filter(gt -> gt.getOwner() != null && gt.getOwner().equals(aiPlayerGame))
            .collect(Collectors.toSet());

    // Encontrar a Fonte e o Destino
    GameTerritory source = findBestFortificationSource(aiTerritories, aiPlayerGame);
    GameTerritory target = findBestFortificationTarget(aiTerritories, aiPlayerGame);

    // Condição de Saída/Falha na Decisão
    if (source == null || target == null || source.getId().equals(target.getId())) {
      System.out.println("IA - Não há movimento de fortificação ideal. Fim do Turno.");

      // Publica a intenção de PASSAR O TURNO
      publishActionIntent(gameId, aiUsername, ActionType.PASS_TURN, null);
      return;
    }

    // Verificar se Fonte e Destino estão conectados
    boolean isAdjacent =
        territoryBorderRepository
            .findByTerritoryIds(source.getTerritory().getId(), target.getTerritory().getId())
            .isPresent();

    if (!isAdjacent) {
      System.out.println(
          "AI - Fonte e Destino não são adjacentes. Publicando intenção de PASSAR TURNO.");
      publishActionIntent(gameId, aiUsername, ActionType.PASS_TURN, null);
      return;
    }

    // Quantidade a Mover (metade do exército da fonte)
    int troopsToMove = source.getStaticArmies() / 2;

    // Deve deixar pelo menos 1 exército na fonte
    int maxMoveable = source.getStaticArmies() - 1;
    troopsToMove = Math.min(troopsToMove, maxMoveable);

    if (troopsToMove < 1) {
      System.out.println(
          "IA - Não há tropas suficientes para mover. Publicando intenção de PASSAR TURNO.");
      publishActionIntent(gameId, aiUsername, ActionType.PASS_TURN, null);
      return;
    }

    try {

      // Cria a intenção de Ação
      AITurnAction intent =
          AITurnAction.builder()
              .type(ActionType.FORTIFY)
              .sourceTerritoryId(String.valueOf(source.getTerritory().getId()))
              .targetTerritoryId(String.valueOf(target.getTerritory().getId()))
              .numberOfArmies(troopsToMove)
              .build();

      System.out.println(
          "IA - Publicando INTENÇÃO de FORTIFICAR: "
              + troopsToMove
              + " de "
              + source.getTerritory().getName()
              + " para "
              + target.getTerritory().getName());

      // Publica o Evento de Intenção de Ação
      publishActionIntent(gameId, aiUsername, ActionType.FORTIFY, intent);

      // Retorna Imediatamente.

    } catch (RuntimeException e) {
      System.err.println("IA falhou ao decidir a fortificação: " + e.getMessage());
      // Em caso de erro, passa o turno para não travar o jogo
      publishActionIntent(gameId, aiUsername, ActionType.PASS_TURN, null);
    }
  }

  @Transactional
  private void executeCardTradePhase(Long gameId, String aiUsername) {
    // Carregar Game e PlayerGame (para tomar a decisão)
    Game game =
        gameRepository
            .findById(gameId)
            .orElseThrow(() -> new RuntimeException("Partida não encontrada para IA."));

    Player aiPlayer =
        game.getPlayers().stream()
            .filter(p -> p.getUsername().equals(aiUsername))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Jogador IA não encontrado."));

    PlayerGame aiPlayerGame =
        playerGameRepository
            .findByGameAndPlayer(game, aiPlayer)
            .orElseThrow(() -> new RuntimeException("PlayerGame da IA não encontrado."));

    Set<PlayerCard> playerCards = aiPlayerGame.getPlayerCards();

    if (playerCards == null || playerCards.size() < 3) {
      System.out.println("IA - Sem cartas suficientes para trocar. Iniciando Reforço.");

      // Publica a intenção de iniciar a fase de Reforço
      executeReinforcementPhase(gameId, aiUsername);
      return;
    }

    // --- DECISÃO DE TROCA ---
    List<Long> cardIdsToTrade = findBestCardSet(playerCards);

    if (cardIdsToTrade.size() == 3) {
      try {
        // Cria a intenção de Ação
        AITurnAction intent =
            AITurnAction.builder().type(ActionType.CARD_TRADE).cardIds(cardIdsToTrade).build();

        System.out.println("IA - Publicando INTENÇÃO de trocar um conjunto de cartas.");

        // Publica o Evento de Intenção de Ação
        publishActionIntent(gameId, aiUsername, ActionType.CARD_TRADE, intent);

        // Retorna Imediatamente.

      } catch (RuntimeException e) {
        System.err.println("IA falhou ao decidir a troca de cartas: " + e.getMessage());
        // Em caso de erro, avança para o Reforço para não travar
        executeReinforcementPhase(gameId, aiUsername);
      }
    } else {
      // Se tem 3+ cartas mas não encontrou um conjunto trocável, passa para o Reforço.
      System.out.println("IA - Não encontrou um conjunto de cartas trocável. Iniciando Reforço.");
      executeReinforcementPhase(gameId, aiUsername);
    }
  }

  // AUXILIARES ==================================

  private int getRemainingArmies(Long gameId, Player aiPlayer) {
    Game game =
        gameRepository
            .findById(gameId)
            .orElseThrow(() -> new RuntimeException("Partida não encontrada."));

    PlayerGame aiPlayerGame =
        playerGameRepository
            .findByGameAndPlayer(game, aiPlayer)
            .orElseThrow(() -> new RuntimeException("PlayerGame da IA não encontrado."));
    return aiPlayerGame.getUnallocatedArmies();
  }

  private GameTerritory findMostVulnerableTerritory(Set<GameTerritory> aiTerritories) {

    GameTerritory mostVulnerable = null;
    double highestVulnerabilityScore = -1.0;

    for (GameTerritory gt : aiTerritories) {
      if (gt.getStaticArmies() <= 1) continue;

      // Encontrar o número de vizinhos inimigos
      long enemyNeighbors =
          gt.getTerritory().getNeighborTerritories().stream()
              .map(
                  neighborTerritory ->
                      gt.getGame().getGameTerritories().stream()
                          .filter(
                              gtN -> gtN.getTerritory().getId().equals(neighborTerritory.getId()))
                          .findFirst()
                          .orElse(null))
              .filter(
                  gtNeighbor ->
                      gtNeighbor != null
                          && gtNeighbor.getOwner() != null
                          && !gtNeighbor.getOwner().equals(gt.getOwner()))
              .count();

      if (enemyNeighbors == 0) continue; // Não é fronteira

      // Cálculo do Score de Vulnerabilidade (quanto maior, mais tropas são necessárias) Score =
      // (Total de Vizinhos Inimigos) / (Tropas Próprias)
      double currentVulnerabilityScore = (double) enemyNeighbors / gt.getStaticArmies();

      if (currentVulnerabilityScore > highestVulnerabilityScore) {
        highestVulnerabilityScore = currentVulnerabilityScore;
        mostVulnerable = gt;
      }
    }

    return mostVulnerable;
  }

  private AttackDecision findBestAttack(Game game, PlayerGame aiPlayerGame) {

    // Obter todos os territórios da IA que podem atacar (têm > 1 exército)
    Set<GameTerritory> attackSources =
        game.getGameTerritories().stream()
            .filter(gt -> gt.getOwner() != null && gt.getOwner().equals(aiPlayerGame))
            .filter(gt -> gt.getStaticArmies() > 1)
            .collect(Collectors.toSet());

    AttackDecision bestDecision = null;
    double highestScore = 0.0;

    // Iterar sobre todos os territórios de origem
    for (GameTerritory source : attackSources) {

      // Iterar sobre todos os vizinhos inimigos
      Set<GameTerritory> enemyNeighbors = findEnemyNeighbors(game, source, aiPlayerGame);

      for (GameTerritory target : enemyNeighbors) {

        // Calcular os Dados e o Score
        int attackingArmies = source.getStaticArmies() - 1;
        int defendingArmies = target.getStaticArmies();

        int numDice = Math.min(3, attackingArmies);

        double currentScore = (double) attackingArmies / defendingArmies;

        if (defendingArmies == 1) {
          currentScore *= 2.0;
        }

        if (currentScore > highestScore && currentScore >= 1.5) {
          highestScore = currentScore;
          bestDecision =
              new AttackDecision(
                  source.getTerritory().getId(), target.getTerritory().getId(), numDice);
        }
      }
    }

    return bestDecision;
  }

  private Set<GameTerritory> findEnemyNeighbors(
      Game game, GameTerritory source, PlayerGame aiPlayerGame) {
    return source.getTerritory().getNeighborTerritories().stream()
        .map(
            neighborTerritory ->
                game.getGameTerritories().stream()
                    .filter(gtN -> gtN.getTerritory().getId().equals(neighborTerritory.getId()))
                    .findFirst()
                    .orElse(null))
        .filter(
            gtNeighbor ->
                gtNeighbor != null
                    && gtNeighbor.getOwner() != null
                    && !gtNeighbor.getOwner().equals(aiPlayerGame))
        .collect(Collectors.toSet());
  }

  private GameTerritory findBestFortificationSource(
      Set<GameTerritory> aiTerritories, PlayerGame aiPlayerGame) {
    // Filtra por territórios internos (que não fazem fronteira com o inimigo)
    // Ordena pelo maior número de tropas.
    return aiTerritories.stream()
        .filter(gt -> isBorderTerritory(gt, aiPlayerGame.getId(), gt.getGame()) == false)
        .max(Comparator.comparing(GameTerritory::getStaticArmies))
        .orElse(null);
  }

  private GameTerritory findBestFortificationTarget(
      Set<GameTerritory> aiTerritories, PlayerGame aiPlayerGame) {
    // Filtra por territórios que são fronteira com o inimigo.
    // Ordena pelo menor número de tropas.
    return aiTerritories.stream()
        .filter(gt -> isBorderTerritory(gt, aiPlayerGame.getId(), gt.getGame()))
        .min(Comparator.comparing(GameTerritory::getStaticArmies))
        .orElse(null);
  }

  private boolean isBorderTerritory(GameTerritory gt, Long aiPlayerGameId, Game game) {
    return gt.getTerritory().getNeighborTerritories().stream()
        .map(
            neighborTerritory ->
                game.getGameTerritories().stream()
                    .filter(gtN -> gtN.getTerritory().getId().equals(neighborTerritory.getId()))
                    .findFirst()
                    .orElse(null))
        .filter(gtNeighbor -> gtNeighbor != null && gtNeighbor.getOwner() != null)
        .anyMatch(gtNeighbor -> !gtNeighbor.getOwner().getId().equals(aiPlayerGameId));
  }

  private List<Long> findBestCardSet(Set<PlayerCard> playerCards) {
    if (playerCards.size() < 3) {
      return List.of();
    }

    // Agrupa as cartas por tipo
    Map<CardType, List<PlayerCard>> cardsByType =
        playerCards.stream().collect(Collectors.groupingBy(pc -> pc.getCard().getType()));

    // Tenta encontrar 3 cartas do mesmo tipo
    for (Map.Entry<CardType, List<PlayerCard>> entry : cardsByType.entrySet()) {
      if (entry.getValue().size() >= 3) {
        return entry.getValue().stream()
            .limit(3)
            .map(PlayerCard::getId)
            .collect(Collectors.toList());
      }
    }

    // Tenta encontrar 3 cartas de tipos diferentes
    List<PlayerCard> infantry = cardsByType.getOrDefault(CardType.INFANTRY, List.of());
    List<PlayerCard> cavalry = cardsByType.getOrDefault(CardType.CAVALRY, List.of());
    List<PlayerCard> artillery = cardsByType.getOrDefault(CardType.CANNON, List.of());

    if (infantry.size() >= 1 && cavalry.size() >= 1 && artillery.size() >= 1) {
      // Encontrou 1 de cada.
      return List.of(infantry.get(0).getId(), cavalry.get(0).getId(), artillery.get(0).getId());
    }

    return List.of();
  }

  private record AttackDecision(Long fromTerritoryId, Long toTerritoryId, int numDice) {}

  // EVENTO ==================================

  // Ouve o feedback de que uma ação foi executada e decide a próxima fase/ação.
  @EventListener
  public void handleAIActionExecuted(AIActionExecutedEvent event) {
    Long gameId = event.getGameId();
    String aiUsername = event.getAiUsername();
    ActionType completedActionType = event.getCompletedActionType();
    boolean turnIsFinished = event.isTurnIsFinished();

    if (turnIsFinished) {
      System.out.println("IA - Turno de " + aiUsername + " COMPLETADO pelo GameService.");
      return;
    }

    // Lógica de Orquestração
    switch (completedActionType) {
      case CARD_TRADE:
        // Se acabou de trocar cartas, decide se troca mais ou passa para o REFORÇO
        executeCardTradePhase(gameId, aiUsername);
        break;
      case REINFORCE_ALLOCATION:
        // Se acabou de alocar, verifica se ainda há exércitos para alocar
        executeReinforcementPhase(gameId, aiUsername);
        break;
      case PASS_PHASE: // Se a fase de reforço terminou
        System.out.println("IA - Transição: Reforço concluído. Iniciando Ataque.");
        // A IA agora PUBLICA a INTENÇÃO de passar para a próxima fase
        publishActionIntent(gameId, aiUsername, ActionType.PASS_PHASE, null);
        // Depois que o GameService mudar a fase, a IA será chamada para o ataque.
        break;
      case ATTACK: // Se um ataque foi executado
        // Decide se ataca novamente ou passa para a fortificação
        executeAttackPhase(gameId, aiUsername);
        break;
      case FORTIFY: // Se a fortificação foi executada
        System.out.println("IA - Fortificação concluída. Fim do turno.");
        // A IA agora PUBLICA a INTENÇÃO de passar o turno
        publishActionIntent(gameId, aiUsername, ActionType.PASS_TURN, null);
        break;
      default:
        // Se foi uma ação unitária (como um ataque), a IA decide a próxima jogada
        System.out.println(
            "IA - Ação " + completedActionType + " executada. Decidindo continuação.");
        break;
    }
  }

  // Auxiliar para publicar a intenção de ação da IA.
  private void publishActionIntent(
      Long gameId, String aiUsername, ActionType type, AITurnAction actionDetails) {
    AITurnAction action =
        actionDetails != null ? actionDetails : AITurnAction.builder().type(type).build();

    eventPublisher.publishEvent(new AIActionIntentEvent(this, gameId, aiUsername, action));
  }
}
