package com.war.game.war_backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.services.GameService;
import com.war.game.war_backend.services.PlayerService;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock private GameService gameService;

    @Mock private PlayerService playerService;

    @Mock private SimpMessagingTemplate messagingTemplate;

    @Mock private Principal principal;

    @InjectMocks private GameController gameController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gameController).setControllerAdvice().build();
    }

    @Test
    void getCurrentTurnInfo_Success_ShouldReturnTurnInfo() throws Exception {
        // Arrange
        Long gameId = 1L;
        String username = "testuser";

        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setUsername(username);
        mockPlayer.setImageUrl("https://example.com/avatar.jpg");

        Player turnPlayer = new Player();
        turnPlayer.setId(2L);
        turnPlayer.setUsername("currentplayer");
        turnPlayer.setImageUrl("https://example.com/current.jpg");

        PlayerGame mockPlayerGame = new PlayerGame();
        mockPlayerGame.setId(10L);
        mockPlayerGame.setPlayer(mockPlayer);
        mockPlayerGame.setStillInGame(true);
        mockPlayerGame.setColor("blue");
        mockPlayerGame.setTurnOrder(1);

        PlayerGame mockTurnPlayerGame = new PlayerGame();
        mockTurnPlayerGame.setId(20L);
        mockTurnPlayerGame.setPlayer(turnPlayer);
        mockTurnPlayerGame.setStillInGame(true);
        mockTurnPlayerGame.setColor("red");
        mockTurnPlayerGame.setTurnOrder(2);
        mockTurnPlayerGame.setUnallocatedArmies(5);
        mockTurnPlayerGame.setConqueredTerritoryThisTurn(false);

        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setName("Test Game");
        mockGame.setStatus("REINFORCEMENT");
        mockGame.setCreatedAt(LocalDateTime.now());
        mockGame.setTurnPlayer(mockTurnPlayerGame);

        List<PlayerGame> playerGames = new ArrayList<>();
        playerGames.add(mockPlayerGame);
        playerGames.add(mockTurnPlayerGame);
        mockGame.setPlayerGames(new java.util.HashSet<>(playerGames));

        when(principal.getName()).thenReturn(username);
        when(playerService.getPlayerByUsername(username)).thenReturn(mockPlayer);
        when(gameService.findGameById(gameId)).thenReturn(mockGame);

        // Act & Assert
        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", gameId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.gameName").value("Test Game"))
                .andExpect(jsonPath("$.gameStatus").value("REINFORCEMENT"))
                .andExpect(jsonPath("$.currentTurnPlayer.username").value("currentplayer"))
                .andExpect(jsonPath("$.currentTurnPlayer.color").value("red"))
                .andExpect(jsonPath("$.currentTurnPlayer.turnOrder").value(2))
                .andExpect(jsonPath("$.currentTurnPlayer.unallocatedArmies").value(5))
                .andExpect(jsonPath("$.currentTurnPlayer.conqueredTerritoryThisTurn").value(false))
                .andExpect(jsonPath("$.isMyTurn").value(false))
                .andExpect(jsonPath("$.totalPlayers").value(2))
                .andExpect(jsonPath("$.activePlayers").value(2));
    }

    @Test
    void getCurrentTurnInfo_IsMyTurn_ShouldReturnTrue() throws Exception {
        // Arrange
        Long gameId = 1L;
        String username = "testuser";

        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setUsername(username);
        mockPlayer.setImageUrl("https://example.com/avatar.jpg");

        PlayerGame mockPlayerGame = new PlayerGame();
        mockPlayerGame.setId(10L);
        mockPlayerGame.setPlayer(mockPlayer);
        mockPlayerGame.setStillInGame(true);
        mockPlayerGame.setColor("blue");
        mockPlayerGame.setTurnOrder(1);
        mockPlayerGame.setUnallocatedArmies(3);
        mockPlayerGame.setConqueredTerritoryThisTurn(true);

        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setName("Test Game");
        mockGame.setStatus("ATTACK");
        mockGame.setCreatedAt(LocalDateTime.now());
        mockGame.setTurnPlayer(mockPlayerGame); // O jogador atual é o do turno

        List<PlayerGame> playerGames = new ArrayList<>();
        playerGames.add(mockPlayerGame);
        mockGame.setPlayerGames(new java.util.HashSet<>(playerGames));

        when(principal.getName()).thenReturn(username);
        when(playerService.getPlayerByUsername(username)).thenReturn(mockPlayer);
        when(gameService.findGameById(gameId)).thenReturn(mockGame);

        // Act & Assert
        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", gameId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isMyTurn").value(true))
                .andExpect(jsonPath("$.currentTurnPlayer.username").value(username))
                .andExpect(jsonPath("$.currentTurnPlayer.conqueredTerritoryThisTurn").value(true));
    }

    @Test
    void getCurrentTurnInfo_PlayerNotInGame_ShouldReturnForbidden() throws Exception {
        // Arrange
        Long gameId = 1L;
        String username = "outsider";

        Player mockPlayer = new Player();
        mockPlayer.setId(99L);
        mockPlayer.setUsername(username);

        Player gamePlayer = new Player();
        gamePlayer.setId(1L);
        gamePlayer.setUsername("insideplayer");

        PlayerGame mockPlayerGame = new PlayerGame();
        mockPlayerGame.setId(10L);
        mockPlayerGame.setPlayer(gamePlayer);
        mockPlayerGame.setStillInGame(true);

        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setName("Test Game");
        mockGame.setStatus("REINFORCEMENT");
        mockGame.setTurnPlayer(mockPlayerGame);

        List<PlayerGame> playerGames = new ArrayList<>();
        playerGames.add(mockPlayerGame);
        mockGame.setPlayerGames(new java.util.HashSet<>(playerGames));

        when(principal.getName()).thenReturn(username);
        when(playerService.getPlayerByUsername(username)).thenReturn(mockPlayer);
        when(gameService.findGameById(gameId)).thenReturn(mockGame);

        // Act & Assert
        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", gameId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentTurnInfo_GameInLobbyStatus_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Long gameId = 1L;
        String username = "testuser";

        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setUsername(username);

        PlayerGame mockPlayerGame = new PlayerGame();
        mockPlayerGame.setId(10L);
        mockPlayerGame.setPlayer(mockPlayer);
        mockPlayerGame.setStillInGame(true);

        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setName("Test Game");
        mockGame.setStatus("LOBBY"); // Status LOBBY

        List<PlayerGame> playerGames = new ArrayList<>();
        playerGames.add(mockPlayerGame);
        mockGame.setPlayerGames(new java.util.HashSet<>(playerGames));

        when(principal.getName()).thenReturn(username);
        when(playerService.getPlayerByUsername(username)).thenReturn(mockPlayer);
        when(gameService.findGameById(gameId)).thenReturn(mockGame);

        // Act & Assert
        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", gameId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCurrentTurnInfo_NoTurnPlayerDefined_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Long gameId = 1L;
        String username = "testuser";

        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setUsername(username);

        PlayerGame mockPlayerGame = new PlayerGame();
        mockPlayerGame.setId(10L);
        mockPlayerGame.setPlayer(mockPlayer);
        mockPlayerGame.setStillInGame(true);

        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setName("Test Game");
        mockGame.setStatus("REINFORCEMENT");
        mockGame.setTurnPlayer(null); // Sem jogador do turno

        List<PlayerGame> playerGames = new ArrayList<>();
        playerGames.add(mockPlayerGame);
        mockGame.setPlayerGames(new java.util.HashSet<>(playerGames));

        when(principal.getName()).thenReturn(username);
        when(playerService.getPlayerByUsername(username)).thenReturn(mockPlayer);
        when(gameService.findGameById(gameId)).thenReturn(mockGame);

        // Act & Assert
        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", gameId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCurrentTurnInfo_WithInactivePlayers_ShouldCountOnlyActive() throws Exception {
        // Arrange
        Long gameId = 1L;
        String username = "testuser";

        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setUsername(username);

        Player turnPlayer = new Player();
        turnPlayer.setId(2L);
        turnPlayer.setUsername("currentplayer");

        Player inactivePlayer = new Player();
        inactivePlayer.setId(3L);
        inactivePlayer.setUsername("inactiveplayer");

        PlayerGame mockPlayerGame = new PlayerGame();
        mockPlayerGame.setId(10L);
        mockPlayerGame.setPlayer(mockPlayer);
        mockPlayerGame.setStillInGame(true);

        PlayerGame mockTurnPlayerGame = new PlayerGame();
        mockTurnPlayerGame.setId(20L);
        mockTurnPlayerGame.setPlayer(turnPlayer);
        mockTurnPlayerGame.setStillInGame(true);
        mockTurnPlayerGame.setColor("red");
        mockTurnPlayerGame.setTurnOrder(1);
        mockTurnPlayerGame.setUnallocatedArmies(0);
        mockTurnPlayerGame.setConqueredTerritoryThisTurn(false);

        PlayerGame mockInactivePlayerGame = new PlayerGame();
        mockInactivePlayerGame.setId(30L);
        mockInactivePlayerGame.setPlayer(inactivePlayer);
        mockInactivePlayerGame.setStillInGame(false); // Jogador inativo

        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setName("Test Game");
        mockGame.setStatus("MOVEMENT");
        mockGame.setTurnPlayer(mockTurnPlayerGame);

        List<PlayerGame> playerGames = new ArrayList<>();
        playerGames.add(mockPlayerGame);
        playerGames.add(mockTurnPlayerGame);
        playerGames.add(mockInactivePlayerGame);
        mockGame.setPlayerGames(new java.util.HashSet<>(playerGames));

        when(principal.getName()).thenReturn(username);
        when(playerService.getPlayerByUsername(username)).thenReturn(mockPlayer);
        when(gameService.findGameById(gameId)).thenReturn(mockGame);

        // Act & Assert
        mockMvc.perform(
                        get("/api/games/{gameId}/current-turn", gameId)
                                .principal(principal)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlayers").value(3))
                .andExpect(jsonPath("$.activePlayers").value(2)); // Apenas 2 ativos
    }
}
