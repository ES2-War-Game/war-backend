package com.war.game.war_backend.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.war.game.war_backend.controller.dto.request.AttackRequestDto;
import com.war.game.war_backend.controller.dto.response.GameResultResponse;
import com.war.game.war_backend.events.GameOverEvent;
import com.war.game.war_backend.model.Card;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.Objective;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerCard;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.model.enums.CardType;
import com.war.game.war_backend.model.enums.GameConstants;
import com.war.game.war_backend.model.enums.GameStatus;
import com.war.game.war_backend.repository.CardRepository;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.GameTerritoryRepository;
import com.war.game.war_backend.repository.ObjectiveRepository;
import com.war.game.war_backend.repository.PlayerCardRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.TerritoryBorderRepository;
import com.war.game.war_backend.repository.TerritoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerGameRepository playerGameRepository;
    private final TerritoryRepository territoryRepository;
    private final ObjectiveRepository objectiveRepository;
    private final GameTerritoryRepository gameTerritoryRepository;
    private final PlayerCardRepository playerCardRepository;
    private final CardRepository cardRepository;
    private final TerritoryBorderRepository territoryBorderRepository;
    private final WinConditionService winConditionService;

    private final SimpMessagingTemplate messagingTemplate; 
    private final PlayerService playerService;
    private final Random random = new Random();

    private static final Map<String, Integer> CONTINENT_BONUSES = Map.of(
        "Asia", 7,
        "North America", 5,
        "Europe", 5,
        "Africa", 3,
        "South America", 2,
        "Oceania", 2
    );

    // LOBBY =======================================

    @Transactional 
    public Game createNewLobby(String lobbyName, Player creator) {
        Game newGame = new Game();
        newGame.setName(lobbyName);
        newGame.setStatus(GameStatus.LOBBY.name()); 
        newGame.setCreatedAt(LocalDateTime.now());

        gameRepository.save(newGame);

        // --- LÓGICA DE ATRIBUIÇÃO DE COR E DADOS DO JOGADOR ---
        
        // O criador do lobby é o primeiro jogador, atribuímos a primeira cor da lista
        String assignedColor = GameConstants.AVAILABLE_COLORS.get(0); 
        
        // Cria a entidade PlayerGame para o criador
        PlayerGame creatorPlayerGame = new PlayerGame();
        creatorPlayerGame.setGame(newGame);
        creatorPlayerGame.setPlayer(creator);
        
        // Configurando as propriedades do PlayerGame
        creatorPlayerGame.setIsOwner(true);
        creatorPlayerGame.setIsReady(false);
        creatorPlayerGame.setStillInGame(true); 

        // Adicionando a cor
        creatorPlayerGame.setColor(assignedColor); 
        
        // Copiando as propriedades do Player para o PlayerGame
        creatorPlayerGame.setUsername(creator.getUsername()); 
        creatorPlayerGame.setImageUrl(creator.getImageUrl()); 

        playerGameRepository.save(creatorPlayerGame);

        return newGame;
    }

    public List<Game> findAllLobbies() {
        return gameRepository.findByStatus(GameStatus.LOBBY.name());
    }

    @Transactional
    public Game addPlayerToLobby(Long lobbyId, Player player) {
        Game game = gameRepository.findById(lobbyId)
                .orElseThrow(() -> new RuntimeException("Lobby não encontrado."));

        if (!GameStatus.LOBBY.name().equals(game.getStatus())) {
            throw new RuntimeException("Não é possível entrar. O jogo já foi iniciado ou tem status inválido.");
        }
        
        // Checagem de limite de jogadores
        Set<PlayerGame> currentPlayers = game.getPlayerGames();
        if (currentPlayers.size() >= GameConstants.MAX_PLAYERS) {
            throw new RuntimeException("Lobby cheio. Número máximo de jogadores alcançado (" + GameConstants.MAX_PLAYERS + ").");
        }

        // Verifica se o jogador já está no lobby
        Optional<PlayerGame> existingPlayerGame = playerGameRepository.findByGameAndPlayer(game, player);
        if (existingPlayerGame.isPresent()) {
            throw new RuntimeException("Jogador já está no lobby.");
        }
        
        // --- LÓGICA DE ATRIBUIÇÃO DE COR ---
        
        // Encontra todas as cores já utilizadas neste jogo
        Set<String> usedColors = currentPlayers.stream()
                                .map(PlayerGame::getColor)
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toSet());

        // Encontra a primeira cor disponível (na ordem de GameConstants.AVAILABLE_COLORS)
        String assignedColor = GameConstants.AVAILABLE_COLORS.stream()
                                .filter(color -> !usedColors.contains(color))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Erro interno: Nenhuma cor disponível."));

        // ------------------------------------

        // Cria a entidade PlayerGame para o novo jogador
        PlayerGame newPlayerGame = new PlayerGame();
        newPlayerGame.setGame(game);
        newPlayerGame.setPlayer(player);
        newPlayerGame.setIsOwner(false);
        newPlayerGame.setIsReady(false);
        newPlayerGame.setStillInGame(true); 
        
        // Adicionando a cor
        newPlayerGame.setColor(assignedColor); 
        
        newPlayerGame.setUsername(player.getUsername()); 
        newPlayerGame.setImageUrl(player.getImageUrl()); 

        playerGameRepository.save(newPlayerGame);

        return game;
    }

    @Transactional
    public Game removePlayerFromLobby(Long lobbyId, Player player) {
        Game game = gameRepository.findById(lobbyId)
                .orElseThrow(() -> new RuntimeException("Lobby não encontrado."));
        
        if (!GameStatus.LOBBY.name().equals(game.getStatus())) {
            throw new RuntimeException("Não é possível sair. O jogo já foi iniciado.");
        }

        // Encontra a entidade PlayerGame para remover
        PlayerGame playerGame = playerGameRepository.findByGameAndPlayer(game, player)
                .orElseThrow(() -> new RuntimeException("Jogador não está no lobby."));

        // Remove a entidade de relacionamento do banco de dados
        playerGameRepository.delete(playerGame);

        // Lógica para o dono: se o dono sair, o próximo vira o dono
        if (playerGame.getIsOwner()) {
            List<PlayerGame> remainingPlayers = playerGameRepository.findByGame(game);
            
            if (!remainingPlayers.isEmpty()) {
                // Define o próximo jogador como novo dono
                PlayerGame newOwner = remainingPlayers.get(0);
                newOwner.setIsOwner(true);
                playerGameRepository.save(newOwner);
                
            } else {
                // Se não houver mais jogadores, o lobby é excluído
                gameRepository.delete(game);
                return null; // Retorna null para sinalizar que o lobby foi excluído
            }
        }
        
        return game;
    }

    // EM JOGO =====================================

    @Transactional
    public Game startGame(Long gameId, String initiatingUsername) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new RuntimeException("Lobby não encontrado."));

        if (!GameStatus.LOBBY.name().equals(game.getStatus())) {
            throw new RuntimeException("O jogo já foi iniciado ou tem status inválido.");
        }

        Player initiatingPlayer = playerService.getPlayerByUsername(initiatingUsername);
        
        List<PlayerGame> playerGames = playerGameRepository.findByGame(game);
        
        // Validação de Dono e Mínimo de Jogadores
        playerGames.stream()
            .filter(PlayerGame::getIsOwner)
            .filter(pg -> pg.getPlayer().equals(initiatingPlayer))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Apenas o dono do lobby pode iniciar a partida."));

        if (playerGames.size() < 2) {
            throw new RuntimeException("São necessários pelo menos 2 jogadores para iniciar o jogo.");
        }

        // --- INÍCIO DA LÓGICA DE DISTRIBUIÇÃO ---

        // Definição da Ordem de Turnos
        Collections.shuffle(playerGames, new Random());
        for (int i = 0; i < playerGames.size(); i++) {
            playerGames.get(i).setTurnOrder(i + 1);
        }
        
        // Cálculo e Atribuição de Tropas
        int initialTroops = calculateInitialTroops(playerGames.size());
        
        for (PlayerGame pg : playerGames) {
            pg.setUnallocatedArmies(initialTroops);
        }

        // Distribuição de Objetivos
        List<Objective> allObjectives = objectiveRepository.findAll();
        Collections.shuffle(allObjectives, new Random());
        
        for (int i = 0; i < playerGames.size(); i++) {
            // Usa o módulo para garantir que objetivos sejam repetidos se houver mais jogadores que objetivos
            playerGames.get(i).setObjective(allObjectives.get(i % allObjectives.size())); 
        }

        // Distribuição de Territórios
        List<Territory> allTerritories = territoryRepository.findAll();
        Collections.shuffle(allTerritories, new Random());
        
        // Assume que distributeTerritories lida com a criação e atribuição inicial de 1 exército em cada território.
        List<GameTerritory> initialGameTerritories = distributeTerritories(game, playerGames, allTerritories);

        // Salva as mudanças
        playerGameRepository.saveAll(playerGames);
        gameTerritoryRepository.saveAll(initialGameTerritories);
        
        PlayerGame firstPlayer = playerGames.stream()
            .filter(pg -> pg.getTurnOrder() == 1)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Erro ao definir o primeiro jogador."));

        game.setStatus(GameStatus.SETUP_ALLOCATION.name());
        
        game.setTurnPlayer(firstPlayer); 

        return gameRepository.save(game);
    }

    @Transactional // A mesma para alocação inicial e de reforço
    public Game allocateTroops(Long gameId, String username, Long territoryId, Integer count) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new RuntimeException("Partida não encontrada."));

        String currentStatus = game.getStatus();
        
        if (!GameStatus.SETUP_ALLOCATION.name().equals(currentStatus) && !GameStatus.REINFORCEMENT.name().equals(currentStatus)) {
            throw new RuntimeException("Não é a fase de alocação de tropas.");
        }
        
        Player player = playerService.getPlayerByUsername(username);
        PlayerGame currentPlayerGame = playerGameRepository.findByGameAndPlayer(game, player)
            .orElseThrow(() -> new RuntimeException("Jogador não está na partida."));

        // Validação de tropas e count
        if (currentPlayerGame.getUnallocatedArmies() < count || count <= 0) {
            throw new RuntimeException("Quantidade de tropas inválida ou superior à sua reserva.");
        }

        // Validação de Turno (apenas para a fase de reforço)
        if (GameStatus.REINFORCEMENT.name().equals(currentStatus) && !game.getTurnPlayer().equals(currentPlayerGame)) {
            throw new RuntimeException("Não é a sua vez de alocar tropas.");
        }

        // ENCONTRAR E VALIDAR O TERRITÓRIO
        GameTerritory gameTerritory = gameTerritoryRepository.findByGameAndTerritoryId(game, territoryId) 
            .orElseThrow(() -> new RuntimeException("Território não encontrado nesta partida."));

        // Validação de Posse
        if (!gameTerritory.getOwner().equals(currentPlayerGame)) {
            throw new RuntimeException("Você só pode colocar tropas em seus próprios territórios.");
        }

        // APLICAR A ALOCAÇÃO
        // Tropas alocadas são sempre estáticas e podem se mover
        gameTerritory.setStaticArmies(gameTerritory.getStaticArmies() + count);
        currentPlayerGame.setUnallocatedArmies(currentPlayerGame.getUnallocatedArmies() - count);

        // LÓGICA DE TRANSIÇÃO DE FASE
        
        // Verifica se a reserva de tropas do jogador zerou
        if (currentPlayerGame.getUnallocatedArmies() == 0) {
            
            if (GameStatus.SETUP_ALLOCATION.name().equals(currentStatus)) {
                
                List<PlayerGame> remainingAllocators = playerGameRepository.findByGame(game).stream()
                    .filter(PlayerGame::getStillInGame) // Checa se ainda está no jogo
                    .filter(pg -> pg.getUnallocatedArmies() > 0)
                    .sorted(Comparator.comparing(PlayerGame::getTurnOrder))
                    .collect(Collectors.toList());

                if (remainingAllocators.isEmpty()) {
                    // TODOS terminaram a alocação inicial. Transição para o 1º turno de Jogo.
                    
                    // Mudar para a fase de REFORÇO do primeiro jogador
                    game.setStatus(GameStatus.REINFORCEMENT.name()); 
                    
                    // O primeiro jogador já foi setado corretamente no startGame, só precisamos confirmar.
                    PlayerGame firstTurnPlayer = playerGameRepository.findByGame(game).stream()
                        .filter(pg -> pg.getTurnOrder() == 1)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Erro ao definir o jogador inicial do jogo."));
                        
                    // O primeiro jogador deve CALCULAR e ATRIBUIR as tropas de REFORÇO
                    int reinforcementTroops = calculateReinforcementTroops(game, firstTurnPlayer);
                    firstTurnPlayer.setUnallocatedArmies(reinforcementTroops); 
                    playerGameRepository.save(firstTurnPlayer); // Salva o reforço calculado
                    
                    game.setTurnPlayer(firstTurnPlayer); // Garante que o turno é dele
                    
                } else {
                    // Passa para o próximo jogador que ainda precisa alocar
                    game.setTurnPlayer(remainingAllocators.get(0));
                }

            } else if (GameStatus.REINFORCEMENT.name().equals(currentStatus)) {
                // O jogador da vez terminou a alocação de reforço. Transição para a FASE DE ATAQUE.
                game.setStatus(GameStatus.ATTACK.name());
            }
        }
        
        gameTerritoryRepository.save(gameTerritory);
        playerGameRepository.save(currentPlayerGame);
        return gameRepository.save(game);
    }

    @Transactional(readOnly = true)
    public int calculateReinforcementTroops(Game game, PlayerGame playerGame) {
        
        // Contar Territórios do Jogador
        List<GameTerritory> controlledTerritories = gameTerritoryRepository.findByGameAndOwner(game, playerGame);
        int totalTerritories = controlledTerritories.size();
        
        // Tropas nº de territórios / 2 com mínimo de 3
        int territoryTroops = Math.max(3, totalTerritories / 2);
        int continentTroops = 0;

        // Contar Bônus de Continentes
        
        // Agrupa os territórios por continente para verificar a posse total
        Map<String, Long> territoriesPerContinent = controlledTerritories.stream()
            .collect(Collectors.groupingBy(
                gt -> gt.getTerritory().getContinent(), // Assumindo que Territory tem getContinent()
                Collectors.counting()
            ));

        // Obtém todos os nomes de continentes únicos do mapa
        List<String> allContinents = territoryRepository.findAll().stream()
            .map(Territory::getContinent)
            .distinct()
            .collect(Collectors.toList());

        for (String continentName : allContinents) {
            // Conta quantos territórios o jogador tem neste continente
            Long playerTerritoryCount = territoriesPerContinent.getOrDefault(continentName, 0L);

            // Conta quantos territórios existem neste continente
            long totalContinentTerritories = territoryRepository.countByContinent(continentName); 
            
            // Checa se o jogador tem todos os tirritórios do continente
            if (playerTerritoryCount == totalContinentTerritories) {
                // Adiciona o bônus fixo daquele continente
                continentTroops += CONTINENT_BONUSES.getOrDefault(continentName, 0);
            }
        }

        return territoryTroops + continentTroops;
    }

    @Transactional
    public Game startNextTurn(Long gameId, String initiatingUsername) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new RuntimeException("Partida não encontrada."));

        String currentStatus = game.getStatus();

        // Checamos se o status é um dos que permite o avanço de turno
        if (GameStatus.LOBBY.name().equals(currentStatus) || 
            GameStatus.SETUP_ALLOCATION.name().equals(currentStatus) || 
            GameStatus.FINISHED.name().equals(currentStatus) ||
            GameStatus.CANCELED.name().equals(currentStatus) ) {
            
            throw new RuntimeException("A ação de encerrar o turno não é válida na fase atual: " + currentStatus);
        }

        // Achar o jogador atual e validar se a chamada é dele
        PlayerGame currentPlayerGame = game.getTurnPlayer();
        if (!currentPlayerGame.getPlayer().getUsername().equals(initiatingUsername)) {
            throw new RuntimeException("Você não tem permissão para encerrar o turno de outro jogador.");
        }
        
        // --- LÓGICA DE TRANSIÇÃO DE FASES ---

        if (GameStatus.REINFORCEMENT.name().equals(currentStatus)) {
            // Se estiver em Reforço, o 'endTurn' avança para o Ataque.
            
            // Regra: O jogador deve alocar todas as tropas antes de avançar para Ataque.
            if (currentPlayerGame.getUnallocatedArmies() > 0) {
                throw new RuntimeException("Você deve alocar todas as suas tropas de reforço (" + currentPlayerGame.getUnallocatedArmies() + ") antes de avançar para a fase de Ataque.");
            }
            
            game.setStatus(GameStatus.ATTACK.name());
        
        } else if (GameStatus.ATTACK.name().equals(currentStatus)) {
            // Se estiver em Ataque, o 'endTurn' avança para Movimentação.
            game.setStatus(GameStatus.MOVEMENT.name());
        
        } else if (GameStatus.MOVEMENT.name().equals(currentStatus)) {
            
            // --- LÓGICA DE FIM DE TURNO E PASSAGEM DE VEZ ---

            // 1. Recompensa de Carta (se conquistou)
            long currentCards = playerCardRepository.countByPlayerGame(currentPlayerGame); 
            
            if (currentPlayerGame.getConqueredTerritoryThisTurn() && currentCards < 5) {
                drawCard(currentPlayerGame);
            }
            
            // 2. Reset de Flag
            currentPlayerGame.setConqueredTerritoryThisTurn(false);
            
            // 3. Buscar os jogadores ativos, ordenados por turnOrder
            List<PlayerGame> activePlayers = playerGameRepository.findByGame(game).stream()
                .filter(PlayerGame::getStillInGame) // <--- FILTRO CRUCIAL
                .sorted(Comparator.comparing(PlayerGame::getTurnOrder))
                .collect(Collectors.toList());

            if (activePlayers.size() <= 1) { 
                PlayerGame winner = activePlayers.stream().findFirst().orElse(null);
                if (winner != null) {
                    throw new RuntimeException("Tentativa de avanço de turno com jogo já finalizado ou com um único jogador ativo.");
                }
                throw new RuntimeException("Erro de estado do jogo. Nenhum jogador ativo para avançar.");
            }
            
            // 4. Determinar o Próximo Jogador Ativo
            
            // Encontrar o índice do jogador atual na lista ATIVA
            int currentPlayerIndex = activePlayers.indexOf(currentPlayerGame);
            
            // O próximo índice na ordem circular dos ativos
            int nextPlayerIndex = (currentPlayerIndex + 1) % activePlayers.size();
            PlayerGame nextPlayerGame = activePlayers.get(nextPlayerIndex);

            // 5. Transição do Turno
            game.setTurnPlayer(nextPlayerGame);

            // 6. Cálculo e Atribuição de Tropas de Reforço
            int reinforcementTroops = calculateReinforcementTroops(game, nextPlayerGame);
            nextPlayerGame.setUnallocatedArmies(reinforcementTroops); 
            
            // 7. Mudar o Status para a fase de Alocação (Início do novo turno)
            game.setStatus(GameStatus.REINFORCEMENT.name()); 

            playerGameRepository.save(currentPlayerGame);
            playerGameRepository.save(nextPlayerGame);
            
        } else {
            throw new RuntimeException("O jogo não está em uma fase de turno conhecida ou a ação não é válida.");
        }
        
        return gameRepository.save(game);
    }

    @Transactional
    public Game tradeCardsForReinforcements(Long gameId, String username, List<Long> playerCardIdsToTrade) {
        // Validações
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Partida não encontrada."));
        Player player = playerService.getPlayerByUsername(username);
        PlayerGame playerGame = playerGameRepository.findByGameAndPlayer(game, player).orElseThrow(() -> new RuntimeException("Jogador não está na partida."));

        if (!game.getTurnPlayer().equals(playerGame)) {
            throw new RuntimeException("Não é o seu turno.");
        }
        if (!"In Game - Reinforcement".equals(game.getStatus())) {
            throw new RuntimeException("Só é permitido trocar cartas na fase de reforço.");
        }
        
        // Busca as entidades PlayerCard e Card
        List<PlayerCard> playerCardsToTrade = playerCardRepository.findByPlayerGameAndIdIn(playerGame, playerCardIdsToTrade);
        
        if (playerCardsToTrade.size() != 3) {
            throw new RuntimeException("Você deve selecionar exatamente 3 cartas para a troca.");
        }
        
        List<Card> cardsToTrade = playerCardsToTrade.stream().map(PlayerCard::getCard).collect(Collectors.toList());
        
        // Valida o Conjunto de Troca
        if (!isTradeSetValid(cardsToTrade)) {
            throw new RuntimeException("O conjunto de cartas não é válido para troca.");
        }

        // Calcula e Atribui Tropas
        int bonusTroops = calculateCardBonus(game);
        
        // Calcula e aplica o bônus de território diretamente
        calculateTerritoryMatchBonus(game, playerGame, cardsToTrade);

        // Adiciona as tropas à reserva do jogador
        playerGame.setUnallocatedArmies(playerGame.getUnallocatedArmies() + bonusTroops);

        // Remove as Cartas
        playerCardRepository.deleteAll(playerCardsToTrade);

        // Atualiza o Contador Global de Trocas
        game.setCardSetExchangeCount(game.getCardSetExchangeCount() + 1);

        return gameRepository.save(game);
    }

    @Transactional
    public Game attackTerritory(Long gameId, String initiatingUsername, AttackRequestDto dto) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Partida não encontrada."));

        if (!GameStatus.ATTACK.name().equals(game.getStatus())) {
            throw new RuntimeException("Ação inválida. A partida não está na fase de Ataque.");
        }

        PlayerGame currentPlayerGame = game.getTurnPlayer();
        if (!currentPlayerGame.getPlayer().getUsername().equals(initiatingUsername)) {
            throw new RuntimeException("Não é o seu turno para atacar.");
        }

        // Busca de Territórios
        GameTerritory sourceTerritory = gameTerritoryRepository.findById(dto.getSourceTerritoryId())
                .orElseThrow(() -> new RuntimeException("Território atacante não encontrado."));
        GameTerritory targetTerritory = gameTerritoryRepository.findById(dto.getTargetTerritoryId())
                .orElseThrow(() -> new RuntimeException("Território defensor não encontrado."));

        // Validação de Posse, Vizinhança e Tropas
        if (!sourceTerritory.getOwner().equals(currentPlayerGame)) {
            throw new RuntimeException("O território atacante não pertence a você.");
        }
        if (targetTerritory.getOwner().equals(currentPlayerGame)) {
            throw new RuntimeException("Você não pode atacar seu próprio território.");
        }
        
        // Checa se existe um registro de fronteira entre os dois territórios mestres.
        boolean isNeighbor = territoryBorderRepository.findByTerritoryIds(
            sourceTerritory.getTerritory().getId(), 
            targetTerritory.getTerritory().getId()
        ).isPresent();
        
        if (!isNeighbor) {
            throw new RuntimeException("O território " + targetTerritory.getTerritory().getName() + " não é vizinho do território atacante.");
        }

        // Validação de Tropas do Atacante e Dados
        // Para ataque, consideramos apenas tropas estáticas (que não se moveram)
        int armiesAvailable = sourceTerritory.getStaticArmies();
        if (armiesAvailable <= dto.getAttackDiceCount()) {
            throw new RuntimeException("Você deve deixar pelo menos um exército no território atacante. Máximo de dados de ataque permitido: " + (armiesAvailable - 1));
        }

        // Determinar Dados de Defesa
        PlayerGame defenderPlayerGame = targetTerritory.getOwner();
        // Para defesa, todas as tropas (estáticas e movidas) podem defender
        int defenseArmies = targetTerritory.getStaticArmies() + targetTerritory.getMovedInArmies();
        
        // Defensor usa 2 dados se tiver 2 ou mais exércitos, senão usa 1.
        int defenseDiceCount = (defenseArmies >= 2) ? 2 : 1;
        
        // Rolagem de Dados e Resolução
        // ... (Simulação e Resolução de Combate)
        List<Integer> attackRolls = simulateDiceRolls(dto.getAttackDiceCount());
        List<Integer> defenseRolls = simulateDiceRolls(defenseDiceCount);
        
        int[] combatResult = resolveCombat(attackRolls, defenseRolls); // [perdas_atacante, perdas_defensor]
        int attackerLosses = combatResult[0];
        int defenderLosses = combatResult[1];

        // Aplicação das Perdas
        // O atacante sempre perde tropas estáticas
        sourceTerritory.setStaticArmies(sourceTerritory.getStaticArmies() - attackerLosses);
        
        // O defensor perde primeiro as tropas estáticas, depois as movidas
        int remainingDefenderLosses = defenderLosses;
        int currentStaticArmies = targetTerritory.getStaticArmies();
        
        if (currentStaticArmies >= remainingDefenderLosses) {
            targetTerritory.setStaticArmies(currentStaticArmies - remainingDefenderLosses);
        } else {
            remainingDefenderLosses -= currentStaticArmies;
            targetTerritory.setStaticArmies(0);
            targetTerritory.setMovedInArmies(targetTerritory.getMovedInArmies() - remainingDefenderLosses);
        }

        // Log (manter log para debug)
        System.out.printf("Combate: %s vs %s. Atacante (%s) perdeu %d. Defensor (%s) perdeu %d.\n",
                sourceTerritory.getTerritory().getName(), 
                targetTerritory.getTerritory().getName(),
                String.join(",", attackRolls.stream().map(Object::toString).toList()),
                attackerLosses,
                String.join(",", defenseRolls.stream().map(Object::toString).toList()),
                defenderLosses);

        gameTerritoryRepository.save(sourceTerritory);
        gameTerritoryRepository.save(targetTerritory);
        
        // Lógica de Conquista
        if ((targetTerritory.getStaticArmies() + targetTerritory.getMovedInArmies()) <= 0) {
            
            // Validação do movimento mínimo
            if (dto.getTroopsToMoveAfterConquest() < dto.getAttackDiceCount() || dto.getTroopsToMoveAfterConquest() >= armiesAvailable) {
                throw new RuntimeException(String.format("Movimento de exércitos inválido após a conquista. Mínimo: %d, Máximo: %d.", dto.getAttackDiceCount(), armiesAvailable - 1));
            }
            
            // Transferência de Posse e Tropas
            targetTerritory.setOwner(currentPlayerGame);
            // Tropas que conquistam um território ficam como moved_in e não podem se mover novamente
            targetTerritory.setStaticArmies(0);
            targetTerritory.setMovedInArmies(dto.getTroopsToMoveAfterConquest());
            
            // Reduz as tropas estáticas do território atacante
            sourceTerritory.setStaticArmies(sourceTerritory.getStaticArmies() - dto.getTroopsToMoveAfterConquest());

            // Setar a flag de carta (Recompensa)
            currentPlayerGame.setConqueredTerritoryThisTurn(true);

            // Salvar
            gameTerritoryRepository.save(targetTerritory);
            gameTerritoryRepository.save(sourceTerritory);
            playerGameRepository.save(currentPlayerGame); // Salva a flag de conquista

            // Checar Fim de Jogo (Isto checa eliminação e, se houver, chama o winConditionService)
            checkGameOver(game, defenderPlayerGame);

            // 2. Checagem de Objetivo Pós-Conquista (NOVA LÓGICA)
            if (!GameStatus.FINISHED.name().equals(game.getStatus())) { 
                // Se o jogo não foi finalizado pelo checkGameOver (após eliminação),
                // checamos se a conquista do território completou o objetivo do atacante.
                winConditionService.checkObjectiveCompletion(game, currentPlayerGame);
            }

            System.out.println("Território conquistado!");
        }
        
        if (GameStatus.FINISHED.name().equals(game.getStatus())) {
            return game; 
        }

        // Se o jogo não terminou notifica e retorna como de costume
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", game);
        return game;
    }

    @Transactional
    public void checkGameOver(Game game, PlayerGame defeatedPlayer) {
        // O atacante é o jogador que está na vez (TurnPlayer)
        PlayerGame attackerPlayer = game.getTurnPlayer(); 
        
        // Verifica se o jogador foi eliminado
        // Assumimos que o countByOwner está corretamente definido no GameTerritoryRepository.
        long territoriesOwned = gameTerritoryRepository.countByOwner(defeatedPlayer);

        if (territoriesOwned == 0) {
            
            // --- Lógica de Transferência de Cartas ---
            
            List<PlayerCard> defeatedCards = playerCardRepository.findByPlayerGame(defeatedPlayer);
            
            // Atualiza a posse de todas as cartas no loop
            for (PlayerCard card : defeatedCards) {
                card.setPlayerGame(attackerPlayer);
            }

            // Persiste todas as mudanças de posse de uma só vez
            if (!defeatedCards.isEmpty()) {
                playerCardRepository.saveAll(defeatedCards); 
                System.out.println(String.format("Transferidas %d cartas de %s para %s.", defeatedCards.size(), defeatedPlayer.getUsername(), attackerPlayer.getUsername()));
            }

            // --- Marcação de Eliminação ---
            
            // Marcar o jogador como 'Eliminado'
            defeatedPlayer.setStillInGame(false); 
            playerGameRepository.save(defeatedPlayer);
            
            System.out.println("Jogador " + defeatedPlayer.getUsername() + " foi eliminado.");
            
            // --- Checagem da Condição de Vitória ---
            
            // Chamar o serviço de verificação de vitória
            winConditionService.checkWinConditions(game, attackerPlayer);
        }
    }

    @EventListener // Anotação crucial para ouvir o evento
    @Transactional // Mantém a garantia de persistência
    public void endGameListener(GameOverEvent event) {
        // Extrair os dados do Evento
        Game game = event.getGame();
        PlayerGame winner = event.getWinner();
        String condition = event.getCondition();
        String description = event.getObjectiveDescription();
        
        // Atualizar o estado do jogo e persistir
        game.setStatus(GameStatus.FINISHED.name()); 
        game.setWinner(winner); 
        gameRepository.save(game);
        
        // Notificação
        GameResultResponse response = new GameResultResponse();
        
        response.setWinningPlayerId(winner.getId());
        response.setWinningPlayerName(winner.getUsername()); 
        response.setWinningPlayerColor(winner.getColor());           
        response.setWinningPlayerImageUrl(winner.getImageUrl()); 
        
        response.setWinningCondition(condition);
        response.setObjectiveDescription(description);
        
        // Envia a notificação via WebSocket
        String topic = "/topic/game/" + game.getId() + "/status"; 
        messagingTemplate.convertAndSend(topic, response);
        
        System.out.println("Jogo " + game.getId() + " finalizado. Vencedor: " + winner.getUsername());
    }

    // AUXILIARES ==================================

    private List<GameTerritory> distributeTerritories(Game game, List<PlayerGame> playerGames, List<Territory> allTerritories) {
        List<GameTerritory> gameTerritories = new java.util.ArrayList<>();
        int playerIndex = 0;

        for (Territory territory : allTerritories) {
            PlayerGame owner = playerGames.get(playerIndex % playerGames.size());
            
            GameTerritory gt = new GameTerritory();
            gt.setGame(game);
            gt.setTerritory(territory);
            gt.setOwner(owner);
            gt.setStaticArmies(1);  // Tropas iniciais são estáticas
            gt.setMovedInArmies(0); // Nenhuma tropa movida inicialmente
            gt.setUnallocatedArmies(0); // Nenhuma tropa não alocada
            
            gameTerritories.add(gt);
            
            playerIndex++;
        }
        return gameTerritories;
    }

    private int calculateInitialTroops(int playerCount) {
        if (playerCount == 2) return 40;
        if (playerCount == 3) return 35;
        if (playerCount == 4) return 30;
        if (playerCount == 5) return 25;
        if (playerCount == 6) return 20;
        return 0;
    }

    private int calculateCardBonus(Game game) {
        int count = game.getCardSetExchangeCount();

        if (count == 0) return 4;
        if (count == 1) return 6;
        if (count == 2) return 8;
        if (count == 3) return 10;
        if (count == 4) return 12;

        // 6ª troca em diante: 15, 20, 25, 30
        // Fórmula: 15 + (exchangeCount - 5) * 5
        return 15 + (count - 5) * 5;
    }

    private boolean isTradeSetValid(List<Card> cardsToTrade) {
        long wildCount = cardsToTrade.stream().filter(c -> c.getType() == CardType.WILD).count();
        
        List<CardType> nonWilds = cardsToTrade.stream()
            .map(Card::getType)
            .filter(t -> t != CardType.WILD)
            .collect(Collectors.toList());

        if (nonWilds.size() == 3 && nonWilds.stream().distinct().count() == 1) {
            return true; 
        }
        
        if (nonWilds.size() == 3 && nonWilds.stream().distinct().count() == 3) {
            return true; 
        }
        
        if (wildCount > 0) {
            if (wildCount == 1) {
                if (nonWilds.stream().distinct().count() == 1) return true;
                if (nonWilds.stream().distinct().count() == 2) return true; 
            }
            
            if (wildCount == 2) return true;

            if (wildCount == 3) return true;
        }
        
        return false;
    }

    private int calculateTerritoryMatchBonus(Game game, PlayerGame playerGame, List<Card> cardsToTrade) {
        int bonus = 0;
        
        // Buscar todos os GameTerritories que o jogador possui nesta partida
        List<GameTerritory> ownedGameTerritories = gameTerritoryRepository.findByGameAndOwner(game, playerGame);

        Map<Long, GameTerritory> ownedTerritoriesMap = ownedGameTerritories.stream()
            .collect(Collectors.toMap(
                gt -> gt.getTerritory().getId(),
                gt -> gt
            ));

        // Verificar o bônus de correspondência de território
        for (Card card : cardsToTrade) {
            if (card.getTerritory() == null) continue;
            Long territoryMasterId = card.getTerritory().getId();
            
            if (ownedTerritoriesMap.containsKey(territoryMasterId)) {
                GameTerritory gt = ownedTerritoriesMap.get(territoryMasterId);
                gt.setStaticArmies(gt.getStaticArmies() + 2);
                gameTerritoryRepository.save(gt);
                bonus += 2;
                System.out.println("Bônus de território para a carta: " + card.getTerritory().getName());
            }
        }
        return bonus;
    }

    private void drawCard(PlayerGame playerGame) {
        // Encontrar a próxima carta disponível no baralho.
        Card cardToDraw = cardRepository.findRandomUnownedCard()
            .orElseThrow(() -> new RuntimeException("Baralho de cartas vazio. Não foi possível comprar carta."));

        // Criar a posse da carta
        PlayerCard playerCard = new PlayerCard();
        playerCard.setPlayerGame(playerGame);
        playerCard.setCard(cardToDraw);

        playerCardRepository.save(playerCard);

        System.out.println("Jogador " + playerGame.getPlayer().getUsername() + " comprou a carta: " + cardToDraw.getType());
    }

    private List<Integer> simulateDiceRolls(int count) {
        List<Integer> rolls = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rolls.add(random.nextInt(6) + 1);
        }
        Collections.sort(rolls, Collections.reverseOrder());
        return rolls;
    }

    private int[] resolveCombat(List<Integer> attackRolls, List<Integer> defenseRolls) {
        int attackerLosses = 0;
        int defenderLosses = 0;
        
        int comparisons = Math.min(attackRolls.size(), defenseRolls.size());
        
        for (int i = 0; i < comparisons; i++) {
            int attackValue = attackRolls.get(i);
            int defenseValue = defenseRolls.get(i);
            
            if (attackValue > defenseValue) {
                // Atacante vence o confronto
                defenderLosses++;
            } else {
                // Defensor vence ou empata (empate é sempre do defensor)
                attackerLosses++;
            }
        }
        
        return new int[]{attackerLosses, defenderLosses};
    }

    public Game findGameById(Long gameId) {
        return gameRepository.findById(gameId)
            .orElseThrow(() -> new RuntimeException("Partida com ID " + gameId + " não encontrada."));
    }
}
