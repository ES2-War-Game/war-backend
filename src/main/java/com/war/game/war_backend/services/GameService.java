package com.war.game.war_backend.services;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.Objective;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.TerritoryRepository;
import com.war.game.war_backend.repository.ObjectiveRepository;
import com.war.game.war_backend.repository.GameTerritoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
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

    private final PlayerService playerService;

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

        if (!"In Game - Running".equals(game.getStatus())) {
            throw new RuntimeException("O jogo não está em andamento (Running).");
        }

        // Achar o jogador atual e o próximo jogador
        PlayerGame currentPlayerGame = game.getTurnPlayer();
        
        // Buscar todos os jogadores ordenados
        List<PlayerGame> allPlayers = playerGameRepository.findByGame(game).stream()
            .sorted(Comparator.comparing(PlayerGame::getTurnOrder))
            .collect(Collectors.toList());

        if (allPlayers.isEmpty()) {
            throw new RuntimeException("Nenhum jogador na partida.");
        }
        
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
        
        // Mudar o Status para a fase de Alocação (Turno é Alocação, Ataque e Movimentação)
        game.setStatus("In Game - Reinforcement"); 

        playerGameRepository.save(nextPlayerGame);
        return gameRepository.save(game);
    }

    // Distribui 1 tropa em cada território para os jogadores.
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
}