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
import com.war.game.war_backend.exceptions.InvalidGamePhaseException;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.GameTerritoryRepository;
import com.war.game.war_backend.repository.ObjectiveRepository;
import com.war.game.war_backend.repository.PlayerCardRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.TerritoryBorderRepository;
import com.war.game.war_backend.repository.TerritoryRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    @PersistenceContext
    private EntityManager entityManager;

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

    // Método auxiliar para remover jogador de lobbies ativos
    @Transactional
    public void removePlayerFromActiveLobbies(Player player) {
        List<PlayerGame> activeLobbies = playerGameRepository.findByPlayerAndGame_Status(player, GameStatus.LOBBY.name());
        
        for (PlayerGame activeLobbyPlayerGame : activeLobbies) {
            Game activeLobby = activeLobbyPlayerGame.getGame();
            boolean wasOwner = activeLobbyPlayerGame.getIsOwner();
            Long lobbyId = activeLobby.getId();
            Long playerGameIdToDelete = activeLobbyPlayerGame.getId();
            
            // Remove da coleção do Game para evitar que o cascade re-persista
            activeLobby.getPlayerGames().remove(activeLobbyPlayerGame);
            
            // Executa o delete nativo (SQL direto) que ignora o cache do Hibernate
            playerGameRepository.deleteByIdNative(playerGameIdToDelete);
            
            // Força flush e limpa o cache do EntityManager
            playerGameRepository.flush();
            entityManager.clear();
            
            // Busca os jogadores restantes diretamente do banco (após o delete)
            List<PlayerGame> remainingPlayers = playerGameRepository.findByGame(activeLobby);
            
            // Se o jogador era dono, transfere a propriedade ou deleta o lobby
            if (wasOwner) {
                if (!remainingPlayers.isEmpty()) {
                    // Define o próximo jogador como novo dono
                    PlayerGame newOwner = remainingPlayers.get(0);
                    newOwner.setIsOwner(true);
                    playerGameRepository.save(newOwner);
                    playerGameRepository.flush();
                    
                    // Envia notificação WebSocket usando os jogadores atualizados do banco
                    notifyLobbyUpdateWithPlayers(lobbyId, remainingPlayers);
                } else {
                    // Se não houver mais jogadores, exclui o lobby
                    gameRepository.deleteById(lobbyId);
                    gameRepository.flush();
                }
            } else {
                // Jogador comum saiu, notifica o lobby usando os jogadores atualizados
                notifyLobbyUpdateWithPlayers(lobbyId, remainingPlayers);
            }
        }
    }
    
    // Método auxiliar para notificar mudanças em um lobby via WebSocket (usando lista de jogadores atualizada)
    private void notifyLobbyUpdateWithPlayers(Long lobbyId, List<PlayerGame> currentPlayers) {
        List<com.war.game.war_backend.controller.dto.response.PlayerLobbyDtoResponse> playerDtos = currentPlayers.stream()
            .map(pg -> new com.war.game.war_backend.controller.dto.response.PlayerLobbyDtoResponse(
                pg.getId(),
                pg.getPlayer().getUsername(),
                pg.getColor(),
                pg.getIsOwner(),
                pg.getPlayer().getImageUrl()
            ))
            .collect(Collectors.toList());
        
        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/state", playerDtos);
    }

    // LOBBY =======================================

    @Transactional 
    public Game createNewLobby(String lobbyName, Player creator) {
        // Remove o jogador de outros lobbies ativos antes de criar um novo
        removePlayerFromActiveLobbies(creator);
        
        // Cria o novo lobby
        Game newGame = new Game();
        newGame.setName(lobbyName);
        newGame.setStatus(GameStatus.LOBBY.name()); 
        newGame.setCreatedAt(LocalDateTime.now());

        gameRepository.save(newGame);

        // O criador do lobby é o primeiro jogador, atribuímos a primeira cor da lista
        String assignedColor = GameConstants.AVAILABLE_COLORS.get(0); 
        
        // Cria a entidade PlayerGame para o criador
        PlayerGame creatorPlayerGame = new PlayerGame();
        creatorPlayerGame.setGame(newGame);
        creatorPlayerGame.setPlayer(creator);
        creatorPlayerGame.setIsOwner(true);
        creatorPlayerGame.setStillInGame(true); 
        creatorPlayerGame.setColor(assignedColor); 
        creatorPlayerGame.setUsername(creator.getUsername()); 
        creatorPlayerGame.setImageUrl(creator.getImageUrl()); 

        playerGameRepository.save(creatorPlayerGame);

        newGame.getPlayerGames().add(creatorPlayerGame);

        return newGame;
    }

    public List<Game> findAllLobbies() {
        return gameRepository.findByStatus(GameStatus.LOBBY.name());
    }

    public Game findCurrentGameForPlayer(Player player) {
        // Busca qualquer jogo ativo do jogador (lobby ou em andamento)
        List<PlayerGame> activeGames = playerGameRepository.findByPlayerAndStillInGame(player, true);
        
        if (activeGames.isEmpty()) {
            return null;
        }
        
        // Retorna o jogo mais recente (último criado)
        return activeGames.stream()
            .map(PlayerGame::getGame)
            .filter(game -> !GameStatus.FINISHED.name().equals(game.getStatus()) 
                         && !GameStatus.CANCELED.name().equals(game.getStatus()))
            .max((g1, g2) -> g1.getCreatedAt().compareTo(g2.getCreatedAt()))
            .orElse(null);
    }

    @Transactional
    public Game addPlayerToLobby(Long lobbyId, Player player) {
        Game game = gameRepository.findById(lobbyId)
                .orElseThrow(() -> new RuntimeException("Lobby não encontrado."));

        if (!GameStatus.LOBBY.name().equals(game.getStatus())) {
            throw new RuntimeException("Não é possível entrar. O jogo já foi iniciado ou tem status inválido.");
        }
        
        // Verifica se o jogador já está neste lobby específico ANTES de consultar outros lobbies
        Optional<PlayerGame> existingPlayerGame = playerGameRepository.findByGameAndPlayer(game, player);
        
        if (existingPlayerGame.isPresent()) {
            return game; // Jogador já está no lobby, retorna sucesso (operação idempotente)
        }
        
        // Remove o jogador de outros lobbies ativos (transação separada)
        removePlayerFromActiveLobbies(player);
        
        // Verifica se o jogador está em algum jogo realmente ativo (não lobby, não finalizado/cancelado)
        Game currentGame = findCurrentGameForPlayer(player);
        
        if (currentGame != null && !GameStatus.LOBBY.name().equals(currentGame.getStatus())) {
            throw new RuntimeException("Você já está em um jogo ativo. Saia do jogo atual antes de entrar em outro lobby.");
        }

        // Checagem de limite de jogadores
        Set<PlayerGame> currentPlayers = game.getPlayerGames();
        
        if (currentPlayers.size() >= GameConstants.MAX_PLAYERS) {
            throw new RuntimeException("Lobby cheio. Número máximo de jogadores alcançado (" + GameConstants.MAX_PLAYERS + ").");
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
        newPlayerGame.setStillInGame(true); 
        
        // Adicionando a cor
        newPlayerGame.setColor(assignedColor); 
        
        newPlayerGame.setUsername(player.getUsername()); 
        newPlayerGame.setImageUrl(player.getImageUrl()); 

        playerGameRepository.save(newPlayerGame);
        
        game.getPlayerGames().add(newPlayerGame);
        
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

        // Reove o player
        game.getPlayerGames().remove(playerGame); 
        
        // Remove a entidade de relacionamento do banco de dados
        playerGameRepository.delete(playerGame);

        // Lógica para o dono: se o dono sair, o próximo vira o dono
        if (playerGame.getIsOwner()) {
            Set<PlayerGame> remainingPlayersSet = game.getPlayerGames();
            
            if (!remainingPlayersSet.isEmpty()) {
                // Converte para lista para pegar o 'primeiro'
                List<PlayerGame> remainingPlayersList = new ArrayList<>(remainingPlayersSet); 
                
                // Define o próximo jogador como novo dono
                PlayerGame newOwner = remainingPlayersList.get(0);
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

    @Transactional
    public Game removePlayerFromGame(Long gameId, Player player) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado."));

        // Encontra o PlayerGame do jogador
        PlayerGame playerGame = playerGameRepository.findByGameAndPlayer(game, player)
                .orElseThrow(() -> new RuntimeException("Jogador não está neste jogo."));

        // Marca o jogador como fora do jogo (stillInGame = false)
        playerGame.setStillInGame(false);
        playerGameRepository.save(playerGame);

        // Se era o turno desse jogador, passa para o próximo
        if (game.getTurnPlayer() != null && 
            game.getTurnPlayer().getId().equals(playerGame.getId())) {
            
            // Busca próximo jogador ativo
            List<PlayerGame> activePlayers = playerGameRepository.findByGame(game).stream()
                    .filter(PlayerGame::getStillInGame)
                    .sorted(Comparator.comparing(PlayerGame::getTurnOrder))
                    .collect(Collectors.toList());

            if (!activePlayers.isEmpty()) {
                // Encontra o próximo jogador na ordem
                int currentIndex = -1;
                for (int i = 0; i < activePlayers.size(); i++) {
                    if (activePlayers.get(i).getTurnOrder() > playerGame.getTurnOrder()) {
                        currentIndex = i;
                        break;
                    }
                }
                
                // Se não encontrou ninguém depois, volta para o primeiro
                if (currentIndex == -1) {
                    currentIndex = 0;
                }
                
                PlayerGame nextPlayer = activePlayers.get(currentIndex);
                game.setTurnPlayer(nextPlayer);
                gameRepository.save(game);
            } else {
                // Não há mais jogadores ativos, finaliza o jogo
                game.setStatus(GameStatus.FINISHED.name());
                gameRepository.save(game);
            }
        }

        // Transfere territórios do jogador que saiu para jogadores ativos
        List<GameTerritory> playerTerritories = gameTerritoryRepository
                .findByGameAndOwner(game, playerGame);
        
        if (!playerTerritories.isEmpty()) {
            List<PlayerGame> activePlayers = playerGameRepository.findByGame(game).stream()
                    .filter(PlayerGame::getStillInGame)
                    .collect(Collectors.toList());
            
            if (!activePlayers.isEmpty()) {
                // Distribui territórios entre jogadores ativos de forma round-robin
                int playerIndex = 0;
                for (GameTerritory territory : playerTerritories) {
                    territory.setOwner(activePlayers.get(playerIndex));
                    gameTerritoryRepository.save(territory);
                    
                    playerIndex = (playerIndex + 1) % activePlayers.size();
                }
            }
        }

        // Verifica se só restou 1 jogador ativo (vencedor)
        long activePlayersCount = playerGameRepository.findByGame(game).stream()
                .filter(PlayerGame::getStillInGame)
                .count();
        
        if (activePlayersCount == 1) {
            // Encontra o vencedor
            PlayerGame winner = playerGameRepository.findByGame(game).stream()
                    .filter(PlayerGame::getStillInGame)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Erro ao determinar vencedor."));
            
            // Dispara evento de vitória por eliminação
            winConditionService.checkWinConditions(game, winner);
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
        System.out.println("\n=== INÍCIO ALOCAÇÃO DE TROPAS ===");
        System.out.println("GameId: " + gameId);
        System.out.println("Username: " + username);
        System.out.println("TerritoryId (recebido): " + territoryId);
        System.out.println("Count: " + count);
        
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new RuntimeException("Partida não encontrada."));

        String currentStatus = game.getStatus();
        System.out.println("Game Status: " + currentStatus);
        
        if (!GameStatus.SETUP_ALLOCATION.name().equals(currentStatus) && !GameStatus.REINFORCEMENT.name().equals(currentStatus)) {
            throw new InvalidGamePhaseException(
                "Não é a fase de alocação de tropas. Fase atual: " + currentStatus,
                currentStatus,
                "SETUP_ALLOCATION ou REINFORCEMENT"
            );
        }
        
        Player player = playerService.getPlayerByUsername(username);
        PlayerGame currentPlayerGame = playerGameRepository.findByGameAndPlayer(game, player)
            .orElseThrow(() -> new RuntimeException("Jogador não está na partida."));

        System.out.println("CurrentPlayerGame ID: " + currentPlayerGame.getId());

        // Validação de tropas e count
        if (currentPlayerGame.getUnallocatedArmies() < count || count <= 0) {
            throw new RuntimeException("Quantidade de tropas inválida ou superior à sua reserva.");
        }

        // Validação de Turno (apenas para a fase de reforço)
        if (GameStatus.REINFORCEMENT.name().equals(currentStatus) && !game.getTurnPlayer().getId().equals(currentPlayerGame.getId())) {
            throw new RuntimeException("Não é a sua vez de alocar tropas.");
        }

        // ENCONTRAR E VALIDAR O TERRITÓRIO
        GameTerritory gameTerritory = gameTerritoryRepository.findByGameAndTerritoryId(game, territoryId) 
            .orElseThrow(() -> new RuntimeException("Território não encontrado nesta partida."));

        System.out.println("\n--- VALIDAÇÃO DE POSSE (ALOCAÇÃO) ---");
        System.out.println("GameTerritory encontrado:");
        System.out.println("  - GameTerritory ID: " + gameTerritory.getId());
        System.out.println("  - Territory ID: " + gameTerritory.getTerritory().getId());
        System.out.println("  - Territory Name: " + gameTerritory.getTerritory().getName());
        System.out.println("  - Owner (PlayerGame) ID: " + gameTerritory.getOwner().getId());
        System.out.println("  - Owner Username: " + gameTerritory.getOwner().getPlayer().getUsername());
        System.out.println("CurrentPlayerGame ID: " + currentPlayerGame.getId());
        System.out.println("IDs iguais? " + gameTerritory.getOwner().getId().equals(currentPlayerGame.getId()));

        // Validação de Posse - Compara IDs ao invés de objetos
        if (!gameTerritory.getOwner().getId().equals(currentPlayerGame.getId())) {
            System.out.println("❌ ERRO: Owner ID (" + gameTerritory.getOwner().getId() + ") != CurrentPlayer ID (" + currentPlayerGame.getId() + ")");
            throw new RuntimeException("Você só pode colocar tropas em seus próprios territórios.");
        }
        
        System.out.println("✅ Validação de posse OK - Alocando " + count + " tropas");

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
            
            throw new InvalidGamePhaseException(
                "A ação de encerrar o turno não é válida na fase atual: " + currentStatus,
                currentStatus,
                "REINFORCEMENT, ATTACK ou MOVEMENT"
            );
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

        // Compara IDs ao invés de objetos
        if (!game.getTurnPlayer().getId().equals(playerGame.getId())) {
            throw new RuntimeException("Não é o seu turno.");
        }
        if (!GameStatus.REINFORCEMENT.name().equals(game.getStatus())) {
            throw new InvalidGamePhaseException(
                "Só é permitido trocar cartas na fase de reforço. Fase atual: " + game.getStatus(),
                game.getStatus(),
                "REINFORCEMENT"
            );
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
        System.out.println("\n=== INÍCIO ATAQUE ===");
        System.out.println("GameId: " + gameId);
        System.out.println("Username: " + initiatingUsername);
        System.out.println("SourceTerritoryId: " + dto.getSourceTerritoryId());
        System.out.println("TargetTerritoryId: " + dto.getTargetTerritoryId());
        System.out.println("AttackDiceCount: " + dto.getAttackDiceCount());
        
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Partida não encontrada."));

        System.out.println("Game encontrado. Status: " + game.getStatus());

        if (!GameStatus.ATTACK.name().equals(game.getStatus())) {
            throw new InvalidGamePhaseException(
                "Ação inválida. A partida não está na fase de Ataque. Fase atual: " + game.getStatus(),
                game.getStatus(),
                "ATTACK"
            );
        }

        PlayerGame currentPlayerGame = game.getTurnPlayer();
        System.out.println("TurnPlayer ID: " + currentPlayerGame.getId());
        System.out.println("TurnPlayer Username: " + currentPlayerGame.getPlayer().getUsername());
        
        if (!currentPlayerGame.getPlayer().getUsername().equals(initiatingUsername)) {
            throw new RuntimeException("Não é o seu turno para atacar.");
        }

        // ✅ CRITICAL FIX: Busca GameTerritory pelo Territory.id (não pelo GameTerritory.id)
        // O frontend envia Territory.id, então precisamos buscar o GameTerritory correspondente no Game
        GameTerritory sourceTerritory = gameTerritoryRepository.findByGame_IdAndTerritory_Id(gameId, dto.getSourceTerritoryId())
                .orElseThrow(() -> new RuntimeException("Território atacante não encontrado."));
        GameTerritory targetTerritory = gameTerritoryRepository.findByGame_IdAndTerritory_Id(gameId, dto.getTargetTerritoryId())
                .orElseThrow(() -> new RuntimeException("Território defensor não encontrado."));

        System.out.println("\n--- VALIDAÇÃO DE POSSE ---");
        System.out.println("Source Territory:");
        System.out.println("  - GameTerritory ID: " + sourceTerritory.getId());
        System.out.println("  - Territory Name: " + sourceTerritory.getTerritory().getName());
        System.out.println("  - Owner (PlayerGame) ID: " + sourceTerritory.getOwner().getId());
        System.out.println("  - Owner Username: " + sourceTerritory.getOwner().getPlayer().getUsername());
        System.out.println("CurrentPlayerGame ID: " + currentPlayerGame.getId());
        System.out.println("IDs iguais? " + sourceTerritory.getOwner().getId().equals(currentPlayerGame.getId()));
        
        // Validação de Posse, Vizinhança e Tropas
        // Compara IDs ao invés de objetos para evitar problemas com cache do EntityManager
        if (!sourceTerritory.getOwner().getId().equals(currentPlayerGame.getId())) {
            System.out.println("❌ ERRO: Owner ID (" + sourceTerritory.getOwner().getId() + ") != CurrentPlayer ID (" + currentPlayerGame.getId() + ")");
            throw new RuntimeException("O território atacante não pertence a você.");
        }
        
        System.out.println("✅ Validação de posse OK");
        
        if (targetTerritory.getOwner().getId().equals(currentPlayerGame.getId())) {
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
        
        // Validação 1: O território atacante deve ter pelo menos 2 tropas (1 para atacar, 1 para ficar)
        if (armiesAvailable < 2) {
            throw new RuntimeException("Você precisa de pelo menos 2 exércitos no território atacante para realizar um ataque.");
        }
        
        // Validação 2: O número de dados deve estar entre 1 e 3
        if (dto.getAttackDiceCount() < 1 || dto.getAttackDiceCount() > 3) {
            throw new RuntimeException("O número de dados de ataque deve estar entre 1 e 3.");
        }
        
        // Validação 3: O número de dados não pode ser maior ou igual ao número de tropas disponíveis
        // (pois pelo menos 1 tropa deve permanecer no território)
        int maxAttackDice = armiesAvailable - 1;
        if (dto.getAttackDiceCount() > maxAttackDice) {
            throw new RuntimeException("Você deve deixar pelo menos um exército no território atacante. Máximo de dados de ataque permitido: " + maxAttackDice);
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
        
        // Lógica de Conquista
        if ((targetTerritory.getStaticArmies() + targetTerritory.getMovedInArmies()) <= 0) {

            // ✅ LÓGICA CORRIGIDA: Mover apenas as tropas que participaram e sobreviveram ao último round
            // Regras aplicadas:
            // - Tropas que participaram do ataque = dto.getAttackDiceCount()
            // - Tropas perdidas do atacante neste round = attackerLosses
            // - Tropas sobreviventes desse round = dto.getAttackDiceCount() - attackerLosses
            // - Deve mover pelo menos 1 tropa (regra da implementação)
            // - Nunca deixar o território atacante vazio (deve permanecer >= 1)

            int sourceStaticAfterLosses = sourceTerritory.getStaticArmies(); // já subtraído attackerLosses acima

            // Tropas que participaram do ataque
            int attackedArmies = dto.getAttackDiceCount();
            // Tropas do atacante perdidas nesse confronto
            int attackerLossesInRound = attackerLosses;

            int survivingAttackers = attackedArmies - attackerLossesInRound;
            // Deve mover pelo menos 1 (regra da implementação)
            int troopsToMove = Math.max(1, survivingAttackers);

            // Máximo que pode mover sem deixar o território vazio
            int maxMoveable = Math.max(0, sourceStaticAfterLosses - 1);

            if (maxMoveable < 1) {
                // Não há tropas suficientes para mover mantendo 1 no território
                throw new RuntimeException("Erro: Não é possível mover tropas para ocupar sem deixar o território atacante vazio.");
            }

            // Ajusta para o máximo permitido caso necessário
            if (troopsToMove > maxMoveable) {
                troopsToMove = maxMoveable;
            }

            System.out.println("🎯 CONQUISTA! Movendo " + troopsToMove + " tropas para " + targetTerritory.getTerritory().getName());
            System.out.println("   - Tropas no território atacante após perdas: " + sourceStaticAfterLosses);
            System.out.println("   - Tropas que participaram do ataque: " + attackedArmies);
            System.out.println("   - Tropas perdidas pelo atacante neste round: " + attackerLossesInRound);
            System.out.println("   - Tropas a mover: " + troopsToMove);
            System.out.println("   - Tropas que ficam no atacante: " + (sourceStaticAfterLosses - troopsToMove));

            // Transferência de Posse e Tropas
            targetTerritory.setOwner(currentPlayerGame);
            targetTerritory.setStaticArmies(0);
            targetTerritory.setMovedInArmies(troopsToMove);

            // Reduz as tropas estáticas do território atacante, deixando pelo menos 1
            sourceTerritory.setStaticArmies(sourceStaticAfterLosses - troopsToMove);

            // Setar a flag de carta (Recompensa)
            currentPlayerGame.setConqueredTerritoryThisTurn(true);

            // Checar Fim de Jogo (Isto checa eliminação e, se houver, chama o winConditionService)
            checkGameOver(game, defenderPlayerGame);

            // 2. Checagem de Objetivo Pós-Conquista (NOVA LÓGICA)
            if (!GameStatus.FINISHED.name().equals(game.getStatus())) {
                winConditionService.checkObjectiveCompletion(game, currentPlayerGame);
            }

            System.out.println("✅ Território conquistado com sucesso!");
        }
        
        // Salvar mudanças (sempre salva, conquista ou não)
        gameTerritoryRepository.save(targetTerritory);
        gameTerritoryRepository.save(sourceTerritory);
        playerGameRepository.save(currentPlayerGame);
        
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

    @EventListener
    @Transactional
    public void endGameListener(GameOverEvent event) {
        // Extrair os dados do Evento
        Game game = event.getGame();
        PlayerGame winner = event.getWinner();
        
        // Atualizar o estado do jogo e persistir
        game.setStatus(GameStatus.FINISHED.name()); 
        game.setWinner(winner); 
        gameRepository.save(game);
        
        System.out.println("Jogo " + game.getId() + " finalizado. Vencedor: " + winner.getUsername());
        
        // Nota: A notificação WebSocket é enviada pelo GameController via /topic/game/{gameId}/state
        // com o GameStateResponseDto completo que já inclui o winner e status FINISHED
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
