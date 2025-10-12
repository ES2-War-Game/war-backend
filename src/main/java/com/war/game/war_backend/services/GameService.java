package com.war.game.war_backend.services;

import com.war.game.war_backend.model.CardType;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.Objective;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerCard;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.controller.dto.request.AttackRequestDto;
import com.war.game.war_backend.model.Card;
import com.war.game.war_backend.repository.CardRepository;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.TerritoryBorderRepository;
import com.war.game.war_backend.repository.TerritoryRepository;
import com.war.game.war_backend.repository.ObjectiveRepository;
import com.war.game.war_backend.repository.PlayerCardRepository;
import com.war.game.war_backend.repository.GameTerritoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

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

    public Game createNewLobby(String lobbyName, Player creator) {
        Game newGame = new Game();
        newGame.setName(lobbyName);
        newGame.setStatus("Lobby"); // O status inicial é "Lobby"
        newGame.setCreatedAt(LocalDateTime.now());

        gameRepository.save(newGame);

        // Cria a entidade PlayerGame para o criador
        PlayerGame creatorPlayerGame = new PlayerGame();
        creatorPlayerGame.setGame(newGame);
        creatorPlayerGame.setPlayer(creator);
        creatorPlayerGame.setIsOwner(true);
        creatorPlayerGame.setIsReady(false);

        playerGameRepository.save(creatorPlayerGame);

        return newGame;
    }

    public List<Game> findAllLobbies() {
        return gameRepository.findByStatus("Lobby");
    }

    @Transactional
    public Game addPlayerToLobby(Long lobbyId, Player player) {
        Game game = gameRepository.findById(lobbyId)
                .orElseThrow(() -> new RuntimeException("Lobby não encontrado."));

        // Verifica se o jogador já está no lobby
        Optional<PlayerGame> existingPlayerGame = playerGameRepository.findByGameAndPlayer(game, player);
        if (existingPlayerGame.isPresent()) {
            throw new RuntimeException("Jogador já está no lobby.");
        }

        // Cria a entidade PlayerGame para o novo jogador
        PlayerGame newPlayerGame = new PlayerGame();
        newPlayerGame.setGame(game);
        newPlayerGame.setPlayer(player);
        newPlayerGame.setIsOwner(false);
        newPlayerGame.setIsReady(false);

        playerGameRepository.save(newPlayerGame);

        return gameRepository.findById(lobbyId)
                .orElseThrow(() -> new RuntimeException("Lobby não encontrado."));
    }

    @Transactional
    public Game removePlayerFromLobby(Long lobbyId, Player player) {
        Game game = gameRepository.findById(lobbyId)
                .orElseThrow(() -> new RuntimeException("Lobby não encontrado."));

        // Encontra a entidade PlayerGame para remover
        PlayerGame playerGame = playerGameRepository.findByGameAndPlayer(game, player)
                .orElseThrow(() -> new RuntimeException("Jogador não está no lobby."));

        // Remove a entidade de relacionamento do banco de dados
        playerGameRepository.delete(playerGame);

        // Lógica para o dono: se o dono sair, o próximo vira o dono
        if (playerGame.getIsOwner()) {
            List<PlayerGame> remainingPlayers = playerGameRepository.findByGame(game);
            if (!remainingPlayers.isEmpty()) {
                PlayerGame newOwner = remainingPlayers.get(0);
                newOwner.setIsOwner(true);
                playerGameRepository.save(newOwner);
            } else {
                // Se não houver mais jogadores, o lobby é excluído
                gameRepository.delete(game);
                return null; // Retorna null para dizxer que o lobby foi excluído
            }
        }
        
        return gameRepository.findById(lobbyId).orElse(null);
    }

    // EM JOGO =====================================

    @Transactional
    public Game startGame(Long gameId, String initiatingUsername) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new RuntimeException("Lobby não encontrado."));

        if (!"Lobby".equals(game.getStatus())) {
            throw new RuntimeException("O jogo já foi iniciado ou tem status inválido.");
        }

        Player initiatingPlayer = playerService.getPlayerByUsername(initiatingUsername);
        
        List<PlayerGame> playerGames = playerGameRepository.findByGame(game);
        
        // Validação de Dono
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
        
        // Cálculo e Atribuição de Nuero de Tropas
        int initialTroops = calculateInitialTroops(playerGames.size());
        
        for (PlayerGame pg : playerGames) {
            pg.setUnallocatedArmies(initialTroops);
        }

        // Distribuição de Objetivos
        List<Objective> allObjectives = objectiveRepository.findAll();
        Collections.shuffle(allObjectives, new Random());
        
        for (int i = 0; i < playerGames.size(); i++) {
            playerGames.get(i).setObjective(allObjectives.get(i % allObjectives.size()));
        }

        // Distribuição de Territórios
        List<Territory> allTerritories = territoryRepository.findAll();
        Collections.shuffle(allTerritories, new Random());
        
        List<GameTerritory> initialGameTerritories = distributeTerritories(game, playerGames, allTerritories);

        // Salva as mudanças
        playerGameRepository.saveAll(playerGames);
        gameTerritoryRepository.saveAll(initialGameTerritories);
        
        PlayerGame firstPlayer = playerGames.stream()
            .filter(pg -> pg.getTurnOrder() == 1)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Erro ao definir o primeiro jogador."));

        game.setStatus("In Game - Initial Allocation");
        game.setTurnPlayer(firstPlayer); 

        return gameRepository.save(game);
    }

    @Transactional // A mesma para alocação inicial e de reforço
    public Game allocateTroops(Long gameId, String username, Long territoryId, Integer count) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new RuntimeException("Partida não encontrada."));

        String status = game.getStatus();
        
        if (!"In Game - Initial Allocation".equals(status) && !"In Game - Reinforcement".equals(status)) {
            throw new RuntimeException("Não é a fase de alocação de tropas.");
        }
        
        Player player = playerService.getPlayerByUsername(username);
        PlayerGame currentPlayerGame = playerGameRepository.findByGameAndPlayer(game, player)
            .orElseThrow(() -> new RuntimeException("Jogador não está na partida."));

        // O jogador deve ter tropas para alocar
        if (currentPlayerGame.getUnallocatedArmies() < count || count <= 0) {
            throw new RuntimeException("Quantidade de tropas inválida ou superior à sua reserva.");
        }

        // Validação de Turno (APENAS para a fase de reforço, na inicial a vez já passa)
        if ("In Game - Reinforcement".equals(status) && !game.getTurnPlayer().equals(currentPlayerGame)) {
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
        gameTerritory.setArmies(gameTerritory.getArmies() + count);
        currentPlayerGame.setUnallocatedArmies(currentPlayerGame.getUnallocatedArmies() - count);

        // LÓGICA DE TRANSIÇÃO DE FASE
        
        // Verifica se a reserva de tropas do jogador zerou
        if (currentPlayerGame.getUnallocatedArmies() == 0) {
            
            if ("In Game - Initial Allocation".equals(status)) {
                List<PlayerGame> remainingAllocators = playerGameRepository.findByGame(game).stream()
                    .filter(pg -> pg.getUnallocatedArmies() > 0)
                    .sorted(Comparator.comparing(PlayerGame::getTurnOrder))
                    .collect(Collectors.toList());

                if (remainingAllocators.isEmpty()) {
                    // Todos terminaram. Transição para o 1º turno real.
                    game.setStatus("In Game - Running");
                    
                    PlayerGame firstTurnPlayer = playerGameRepository.findByGame(game).stream()
                        .filter(pg -> pg.getTurnOrder() == 1)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Erro ao definir o jogador inicial do jogo."));
                        
                    game.setTurnPlayer(firstTurnPlayer);
                } else {
                    // Passa para o próximo jogador na alocação inicial
                    game.setTurnPlayer(remainingAllocators.get(0));
                }

            } else if ("In Game - Reinforcement".equals(status)) {
                // O jogador da vez terminou a alocação de reforço. Transição para a FASE DE ATAQUE.
                game.setStatus("In Game - Attack");
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

        // Validação inicial para verificar se o jogo está ativo
        if (!game.getStatus().startsWith("In Game")) {
            throw new RuntimeException("O jogo não está em andamento.");
        }

        // Achar o jogador atual e validar se a chamada é dele
        PlayerGame currentPlayerGame = game.getTurnPlayer();
        if (!currentPlayerGame.getPlayer().getUsername().equals(initiatingUsername)) {
            throw new RuntimeException("Você não tem permissão para encerrar o turno de outro jogador.");
        }
        
        // Buscar todos os jogadores ordenados
        List<PlayerGame> allPlayers = playerGameRepository.findByGame(game).stream()
            .sorted(Comparator.comparing(PlayerGame::getTurnOrder))
            .collect(Collectors.toList());

        if (allPlayers.isEmpty()) {
            throw new RuntimeException("Nenhum jogador na partida.");
        }
        
        // --- LÓGICA DE TRANSIÇÃO DE FASES ---
        
        String currentStatus = game.getStatus();

        if ("In Game - Reinforcement".equals(currentStatus)) {
            // Se estiver em Reforço, o 'endTurn' avança para o Ataque.
            
            // Regra: O jogador deve alocar todas as tropas antes de avançar para Ataque.
            if (currentPlayerGame.getUnallocatedArmies() > 0) {
                throw new RuntimeException("Você deve alocar todas as suas tropas de reforço (" + currentPlayerGame.getUnallocatedArmies() + ") antes de avançar para a fase de Ataque.");
            }
            
            game.setStatus("In Game - Attack");
        
        } else if ("In Game - Attack".equals(currentStatus)) {
            // Se estiver em Ataque, o 'endTurn' avança para Movimentação.
            game.setStatus("In Game - Movement");
        
        } else if ("In Game - Movement".equals(currentStatus)) {
            // Se estiver em Movimentação, o 'endTurn' encerra o turno e passa para o próximo jogador.
            
            long currentCards = playerCardRepository.countByPlayerGame(currentPlayerGame); 
            
            if (currentPlayerGame.getConqueredTerritoryThisTurn() && currentCards < 5) {
                // Se conquistou pelo menos 1 território e não está no limite de 5 cartas, recebe uma
                drawCard(currentPlayerGame);
            }
            
            // O jogador que inicia o turno deve ter a flag zerada
            currentPlayerGame.setConqueredTerritoryThisTurn(false);
            
            // Encontrar o índice do jogador atual
            int currentPlayerIndex = allPlayers.indexOf(currentPlayerGame);
            
            // O próximo índice na ordem circular
            int nextPlayerIndex = (currentPlayerIndex + 1) % allPlayers.size();
            PlayerGame nextPlayerGame = allPlayers.get(nextPlayerIndex);

            // Transição do Turno
            game.setTurnPlayer(nextPlayerGame);

            // Cálculo e Atribuição de Tropas
            int reinforcementTroops = calculateReinforcementTroops(game, nextPlayerGame);
            
            // Reutilizando o campo de alocação de tropas
            nextPlayerGame.setUnallocatedArmies(reinforcementTroops); 
            
            // Mudar o Status para a fase de Alocação (Início do novo turno)
            game.setStatus("In Game - Reinforcement"); 

            playerGameRepository.save(currentPlayerGame);
            playerGameRepository.save(nextPlayerGame);
            
        } else if ("Lobby".equals(currentStatus) || "In Game - Initial Allocation".equals(currentStatus) || "Game Over".equals(currentStatus)) {
            // Se o jogo está em Initial Allocation, o 'endTurn' não é usado,
            // e se estiver em Lobby/Game Over, também não deve ser chamado.
            throw new RuntimeException("A ação de encerrar o turno não é válida na fase atual: " + currentStatus);
        } else {
            throw new RuntimeException("O jogo não está em uma fase de turno conhecida.");
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
        
        // Bônus por território, Pode deixar o aviso aqui mesmo, talvez façamos algo com esse valor depois
        int territoryBonus = calculateTerritoryMatchBonus(game, playerGame, cardsToTrade);

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

        // Validação do Estado e Jogador
        if (!"In Game - Attack".equals(game.getStatus())) {
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
        int armiesAvailable = sourceTerritory.getArmies();
        if (armiesAvailable <= dto.getAttackDiceCount()) {
            throw new RuntimeException("Você deve deixar pelo menos um exército no território atacante. Máximo de dados de ataque permitido: " + (armiesAvailable - 1));
        }

        // Determinar Dados de Defesa
        PlayerGame defenderPlayerGame = targetTerritory.getOwner();
        int defenseArmies = targetTerritory.getArmies();
        
        // Defensor usa 2 dados se tiver 2 ou mais exércitos, senão usa 1.
        int defenseDiceCount = (defenseArmies >= 2) ? 2 : 1;
        
        // Rolagem de Dados e Resolução
        List<Integer> attackRolls = simulateDiceRolls(dto.getAttackDiceCount());
        List<Integer> defenseRolls = simulateDiceRolls(defenseDiceCount);
        
        int[] combatResult = resolveCombat(attackRolls, defenseRolls); // [perdas_atacante, perdas_defensor]
        int attackerLosses = combatResult[0];
        int defenderLosses = combatResult[1];

        // Aplicação das Perdas
        sourceTerritory.setArmies(sourceTerritory.getArmies() - attackerLosses);
        targetTerritory.setArmies(targetTerritory.getArmies() - defenderLosses);

        // Log (Só pra ver o que aconteceu, podemos mostrar no front depois)
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
        if (targetTerritory.getArmies() <= 0) {
            
            // Validação do movimento mínimo
            if (dto.getTroopsToMoveAfterConquest() < dto.getAttackDiceCount() || dto.getTroopsToMoveAfterConquest() >= armiesAvailable) {
                // A regra é mover no mínimo o número de dados usados, e no máximo (exércitos disponíveis - 1)
                throw new RuntimeException(String.format("Movimento de exércitos inválido após a conquista. Mínimo: %d, Máximo: %d.", dto.getAttackDiceCount(), armiesAvailable - 1));
            }
            
            // Transferência de Posse e Tropas
            targetTerritory.setOwner(currentPlayerGame);
            targetTerritory.setArmies(dto.getTroopsToMoveAfterConquest());
            
            sourceTerritory.setArmies(sourceTerritory.getArmies() - dto.getTroopsToMoveAfterConquest());

            // Setar a flag de carta (Recompensa)
            currentPlayerGame.setConqueredTerritoryThisTurn(true);

            // Salvar
            gameTerritoryRepository.save(targetTerritory);
            gameTerritoryRepository.save(sourceTerritory);
            playerGameRepository.save(currentPlayerGame); // Salva a flag de conquista

            // Checar Fim de Jogo
            checkGameOver(game, defenderPlayerGame);

            System.out.println("Território conquistado!");
        }

        // Notificação e retorno como de costume
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", game);
        return game;
    }

    // Verifica se o defensor perdeu todos os territórios e, se sim, o remove do jogo e o atacante herda suas cartas.
    private void checkGameOver(Game game, PlayerGame defeatedPlayer) {
        long territoriesOwned = gameTerritoryRepository.countByOwner(defeatedPlayer);

        if (territoriesOwned == 0) {
            // O jogador defensor foi eliminado. O atacante herda suas cartas.
            
            List<PlayerCard> defeatedCards = playerCardRepository.findByPlayerGame(defeatedPlayer);
            for (PlayerCard card : defeatedCards) {
                card.setPlayerGame(game.getTurnPlayer());
                playerCardRepository.save(card);
            }
            
            // Marcar o jogador como 'Derrotado' (ou similar) e removê-lo da lista de ativos do jogo.
            // Para simplificar, podemos apenas remover o jogador do jogo ou setar um status.
            defeatedPlayer.setStillInGame(false); // Assumindo que você tem essa flag
            playerGameRepository.save(defeatedPlayer);
            
            System.out.println("Jogador " + defeatedPlayer.getPlayer().getUsername() + " foi eliminado. Cartas transferidas.");
            
            // Checar se restou apenas um jogador
            // ... (Lógica de fim de jogo final)
        }
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
            gt.setArmies(1);
            
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
            if (card.getTerritory() != null) {
                Long territoryMasterId = card.getTerritory().getId();
                
                if (ownedTerritoriesMap.containsKey(territoryMasterId)) {
                    GameTerritory gt = ownedTerritoriesMap.get(territoryMasterId);
                    gt.setArmies(gt.getArmies() + 2);
                    gameTerritoryRepository.save(gt);
                    bonus += 2; 
                }
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
}