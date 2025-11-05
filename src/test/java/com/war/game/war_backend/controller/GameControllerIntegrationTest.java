package com.war.game.war_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.war.game.war_backend.config.BaseTestConfiguration;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.model.Role;
import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.GameTerritoryRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.PlayerRepository;
import com.war.game.war_backend.repository.RoleRepository;
import com.war.game.war_backend.repository.TerritoryRepository;
import com.war.game.war_backend.security.jwt.JwtTokenUtil;
import java.time.LocalDateTime;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(BaseTestConfiguration.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GameControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private PlayerRepository playerRepository;

    @Autowired private RoleRepository roleRepository;

    @Autowired private GameRepository gameRepository;

    @Autowired private PlayerGameRepository playerGameRepository;

    @Autowired private TerritoryRepository territoryRepository;

    @Autowired private GameTerritoryRepository gameTerritoryRepository;

    @Autowired private JwtTokenUtil jwtTokenUtil;

    private Player player1;
    private Player player2;
    private Player player3;
    private Game game;
    private PlayerGame playerGame1;
    private PlayerGame playerGame2;
    private PlayerGame playerGame3;
    private String jwtToken1;
    private String jwtToken2;

    @BeforeEach
    void setUp() {
        // Get or Create Role
        Role userRole =
                roleRepository
                        .findByName("ROLE_USER")
                        .orElseGet(
                                () -> {
                                    Role role = new Role();
                                    role.setName("ROLE_USER");
                                    return roleRepository.save(role);
                                });

        // Create Players
        player1 = new Player();
        player1.setUsername("player1");
        player1.setPassword("password1");
        player1.setEmail("player1@test.com");
        player1.setImageUrl("https://example.com/player1.jpg");
        player1.setRoles(new HashSet<>());
        player1.getRoles().add(userRole);
        player1 = playerRepository.save(player1);

        player2 = new Player();
        player2.setUsername("player2");
        player2.setPassword("password2");
        player2.setEmail("player2@test.com");
        player2.setImageUrl("https://example.com/player2.jpg");
        player2.setRoles(new HashSet<>());
        player2.getRoles().add(userRole);
        player2 = playerRepository.save(player2);

        player3 = new Player();
        player3.setUsername("player3");
        player3.setPassword("password3");
        player3.setEmail("player3@test.com");
        player3.setImageUrl("https://example.com/player3.jpg");
        player3.setRoles(new HashSet<>());
        player3.getRoles().add(userRole);
        player3 = playerRepository.save(player3);

        // Create Game
        game = new Game();
        game.setName("Integration Test Game");
        game.setStatus("REINFORCEMENT");
        game.setCreatedAt(LocalDateTime.now());
        game = gameRepository.save(game);

        // Create PlayerGames
        playerGame1 = new PlayerGame();
        playerGame1.setPlayer(player1);
        playerGame1.setGame(game);
        playerGame1.setUsername(player1.getUsername());
        playerGame1.setColor("blue");
        playerGame1.setTurnOrder(1);
        playerGame1.setStillInGame(true);
        playerGame1.setUnallocatedArmies(5);
        playerGame1.setConqueredTerritoryThisTurn(false);
        playerGame1 = playerGameRepository.save(playerGame1);

        playerGame2 = new PlayerGame();
        playerGame2.setPlayer(player2);
        playerGame2.setGame(game);
        playerGame2.setUsername(player2.getUsername());
        playerGame2.setColor("red");
        playerGame2.setTurnOrder(2);
        playerGame2.setStillInGame(true);
        playerGame2.setUnallocatedArmies(3);
        playerGame2.setConqueredTerritoryThisTurn(true);
        playerGame2 = playerGameRepository.save(playerGame2);

        playerGame3 = new PlayerGame();
        playerGame3.setPlayer(player3);
        playerGame3.setGame(game);
        playerGame3.setUsername(player3.getUsername());
        playerGame3.setColor("green");
        playerGame3.setTurnOrder(3);
        playerGame3.setStillInGame(false); // Jogador eliminado
        playerGame3.setUnallocatedArmies(0);
        playerGame3.setConqueredTerritoryThisTurn(false);
        playerGame3 = playerGameRepository.save(playerGame3);

        // Manually add playerGames to game's collection (bidirectional relationship)
        game.getPlayerGames().add(playerGame1);
        game.getPlayerGames().add(playerGame2);
        game.getPlayerGames().add(playerGame3);

        // Set turn player and reload game to ensure playerGames are loaded
        game.setTurnPlayer(playerGame1);
        game = gameRepository.save(game);

        // Force flush to ensure all entities are persisted
        gameRepository.flush();
        playerGameRepository.flush();

        // Reload game from database to ensure playerGames collection is populated
        game = gameRepository.findById(game.getId()).orElseThrow();

        // Create Territories and GameTerritories
        Territory territory1 = new Territory();
        territory1.setName("Brazil");
        territory1.setContinent("South America");
        territory1 = territoryRepository.save(territory1);

        Territory territory2 = new Territory();
        territory2.setName("Argentina");
        territory2.setContinent("South America");
        territory2 = territoryRepository.save(territory2);

        GameTerritory gameTerritory1 = new GameTerritory();
        gameTerritory1.setGame(game);
        gameTerritory1.setTerritory(territory1);
        gameTerritory1.setOwner(playerGame1);
        gameTerritory1.setStaticArmies(10);
        gameTerritoryRepository.save(gameTerritory1);

        GameTerritory gameTerritory2 = new GameTerritory();
        gameTerritory2.setGame(game);
        gameTerritory2.setTerritory(territory2);
        gameTerritory2.setOwner(playerGame2);
        gameTerritory2.setStaticArmies(8);
        gameTerritoryRepository.save(gameTerritory2);

        // Generate JWT tokens
        org.springframework.security.core.userdetails.UserDetails userDetails1 =
                org.springframework.security.core.userdetails.User.withUsername(
                                player1.getUsername())
                        .password(player1.getPassword())
                        .authorities("USER")
                        .build();
        jwtToken1 = "Bearer " + jwtTokenUtil.generateToken(userDetails1);

        org.springframework.security.core.userdetails.UserDetails userDetails2 =
                org.springframework.security.core.userdetails.User.withUsername(
                                player2.getUsername())
                        .password(player2.getPassword())
                        .authorities("USER")
                        .build();
        jwtToken2 = "Bearer " + jwtTokenUtil.generateToken(userDetails2);
    }

    @Test
    void getCurrentTurnInfo_AuthenticatedPlayer_ShouldReturnTurnInfo() throws Exception {
        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", game.getId())
                                .header("Authorization", jwtToken1)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(game.getId()))
                .andExpect(jsonPath("$.gameName").value("Integration Test Game"))
                .andExpect(jsonPath("$.gameStatus").value("REINFORCEMENT"))
                .andExpect(jsonPath("$.currentTurnPlayer.username").value("player1"))
                .andExpect(jsonPath("$.currentTurnPlayer.color").value("blue"))
                .andExpect(jsonPath("$.currentTurnPlayer.turnOrder").value(1))
                .andExpect(jsonPath("$.currentTurnPlayer.unallocatedArmies").value(5))
                .andExpect(jsonPath("$.currentTurnPlayer.conqueredTerritoryThisTurn").value(false))
                .andExpect(jsonPath("$.isMyTurn").value(true))
                .andExpect(jsonPath("$.totalPlayers").value(3))
                .andExpect(jsonPath("$.activePlayers").value(2));
    }

    @Test
    void getCurrentTurnInfo_PlayerNotInTurn_ShouldReturnFalseForIsMyTurn() throws Exception {
        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", game.getId())
                                .header("Authorization", jwtToken2)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTurnPlayer.username").value("player1"))
                .andExpect(jsonPath("$.isMyTurn").value(false));
    }

    @Test
    void getCurrentTurnInfo_UnauthenticatedRequest_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", game.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentTurnInfo_PlayerNotInGame_ShouldReturnForbidden() throws Exception {
        // Create a player not in the game
        Player outsider = new Player();
        outsider.setUsername("outsider");
        outsider.setPassword("password");
        outsider.setEmail("outsider@test.com");
        outsider.setImageUrl("https://example.com/outsider.jpg");
        outsider.setRoles(new HashSet<>());
        outsider.getRoles().add(roleRepository.findByName("ROLE_USER").orElseThrow());
        outsider = playerRepository.save(outsider);

        org.springframework.security.core.userdetails.UserDetails outsiderDetails =
                org.springframework.security.core.userdetails.User.withUsername(
                                outsider.getUsername())
                        .password(outsider.getPassword())
                        .authorities("USER")
                        .build();
        String outsiderToken = "Bearer " + jwtTokenUtil.generateToken(outsiderDetails);

        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", game.getId())
                                .header("Authorization", outsiderToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentTurnInfo_GameInLobbyStatus_ShouldReturnBadRequest() throws Exception {
        // Create a game in LOBBY status
        Game lobbyGame = new Game();
        lobbyGame.setName("Lobby Game");
        lobbyGame.setStatus("LOBBY");
        lobbyGame.setCreatedAt(LocalDateTime.now());
        lobbyGame = gameRepository.save(lobbyGame);

        PlayerGame lobbyPlayerGame = new PlayerGame();
        lobbyPlayerGame.setPlayer(player1);
        lobbyPlayerGame.setGame(lobbyGame);
        lobbyPlayerGame.setUsername(player1.getUsername());
        lobbyPlayerGame.setColor("yellow");
        lobbyPlayerGame.setTurnOrder(1);
        lobbyPlayerGame.setStillInGame(true);
        lobbyPlayerGame = playerGameRepository.save(lobbyPlayerGame);

        // Add to game's collection
        lobbyGame.getPlayerGames().add(lobbyPlayerGame);
        lobbyGame = gameRepository.save(lobbyGame);

        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", lobbyGame.getId())
                                .header("Authorization", jwtToken1)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCurrentTurnInfo_NoTurnPlayerDefined_ShouldReturnBadRequest() throws Exception {
        // Create a game without turn player
        Game noTurnGame = new Game();
        noTurnGame.setName("No Turn Game");
        noTurnGame.setStatus("REINFORCEMENT");
        noTurnGame.setCreatedAt(LocalDateTime.now());
        noTurnGame.setTurnPlayer(null); // No turn player
        noTurnGame = gameRepository.save(noTurnGame);

        PlayerGame noTurnPlayerGame = new PlayerGame();
        noTurnPlayerGame.setPlayer(player1);
        noTurnPlayerGame.setGame(noTurnGame);
        noTurnPlayerGame.setUsername(player1.getUsername());
        noTurnPlayerGame.setColor("purple");
        noTurnPlayerGame.setTurnOrder(1);
        noTurnPlayerGame.setStillInGame(true);
        noTurnPlayerGame = playerGameRepository.save(noTurnPlayerGame);

        // Add to game's collection
        noTurnGame.getPlayerGames().add(noTurnPlayerGame);
        noTurnGame = gameRepository.save(noTurnGame);

        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", noTurnGame.getId())
                                .header("Authorization", jwtToken1)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCurrentTurnInfo_GameWithChangedTurnPlayer_ShouldReturnUpdatedInfo() throws Exception {
        // Change turn player to player2
        game.setTurnPlayer(playerGame2);
        game.setStatus("ATTACK");
        gameRepository.save(game);

        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", game.getId())
                                .header("Authorization", jwtToken1)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameStatus").value("ATTACK"))
                .andExpect(jsonPath("$.currentTurnPlayer.username").value("player2"))
                .andExpect(jsonPath("$.currentTurnPlayer.color").value("red"))
                .andExpect(jsonPath("$.currentTurnPlayer.conqueredTerritoryThisTurn").value(true))
                .andExpect(jsonPath("$.isMyTurn").value(false));
    }

    @Test
    void getCurrentTurnInfo_VerifyActivePlayersCount_ShouldExcludeEliminated() throws Exception {
        // Verify that player3 (eliminated) is not counted in active players
        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", game.getId())
                                .header("Authorization", jwtToken1)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlayers").value(3))
                .andExpect(jsonPath("$.activePlayers").value(2)); // player1 and player2 only
    }
}
