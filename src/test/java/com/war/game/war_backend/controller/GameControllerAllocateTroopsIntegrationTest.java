package com.war.game.war_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.GameTerritoryRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.PlayerRepository;
import com.war.game.war_backend.repository.TerritoryRepository;
import com.war.game.war_backend.security.jwt.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class GameControllerAllocateTroopsIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private GameRepository gameRepository;

  @Autowired
  private PlayerRepository playerRepository;

  @Autowired
  private TerritoryRepository territoryRepository;

  @Autowired
  private PlayerGameRepository playerGameRepository;

  @Autowired
  private GameTerritoryRepository gameTerritoryRepository;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private Player testPlayer;
  private Game testGame;
  private Territory testTerritory;
  private PlayerGame testPlayerGame;
  private GameTerritory testGameTerritory;
  private String authToken;

  @BeforeEach
  void setUp() {
    // Criar jogador de teste
    testPlayer = new Player();
    testPlayer.setUsername("testuser");
    testPlayer.setEmail("test@example.com");
    testPlayer.setPassword(passwordEncoder.encode("password"));
    testPlayer.setRoles(new java.util.HashSet<>());
    testPlayer = playerRepository.save(testPlayer);

    // Buscar território existente criado pelo TerritoryInitializer
    testTerritory = territoryRepository.findByName("BRASIL")
        .orElseThrow(() -> new RuntimeException("Território BRASIL não foi inicializado"));

    // Criar jogo de teste
    testGame = new Game();
    testGame.setName("Test Game");
    testGame.setStatus("In Game - Initial Allocation");
    testGame.setCreatedAt(java.time.LocalDateTime.now());
    testGame = gameRepository.save(testGame);

    // Criar PlayerGame (jogador participando do jogo)
    testPlayerGame = new PlayerGame();
    testPlayerGame.setGame(testGame);
    testPlayerGame.setPlayer(testPlayer);
    testPlayerGame.setTurnOrder(1);
    testPlayerGame.setUnallocatedArmies(10); // Tropas disponíveis para alocar
    testPlayerGame.setConqueredTerritoryThisTurn(false);
    testPlayerGame.setStillInGame(true);
    testPlayerGame = playerGameRepository.save(testPlayerGame);

    // Configurar o jogo para ter o jogador como jogador da vez
    testGame.setTurnPlayer(testPlayerGame);
    testGame = gameRepository.save(testGame);

    // Recarregar o jogo do banco para ter as associações atualizadas
    testGame = gameRepository.findById(testGame.getId()).orElseThrow();

    // Criar GameTerritory (território pertencente ao jogador no jogo)
    testGameTerritory = new GameTerritory();
    testGameTerritory.setGame(testGame);
    testGameTerritory.setTerritory(testTerritory);
    testGameTerritory.setOwner(testPlayerGame);
    testGameTerritory.setArmies(5); // Já tem 5 tropas no território
    testGameTerritory = gameTerritoryRepository.save(testGameTerritory);

    // Gerar token JWT
    org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User
        .withUsername(testPlayer.getUsername())
        .password(testPlayer.getPassword())
        .authorities("USER")
        .build();
    authToken = "Bearer " + jwtTokenUtil.generateToken(userDetails);
  }

  @Test
  void allocateTroops_WithValidData_ShouldReturnSuccess() throws Exception {
    // Arrange
    Long territoryId = testTerritory.getId();
    Integer count = 3;

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/allocate", testGame.getId())
        .param("territoryId", territoryId.toString())
        .param("count", count.toString())
        .header("Authorization", authToken)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testGame.getId()))
        .andExpect(jsonPath("$.status").exists());
  }

  @Test
  void allocateTroops_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
    // Arrange
    Long territoryId = testTerritory.getId();
    Integer count = 3;

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/allocate", testGame.getId())
        .param("territoryId", territoryId.toString())
        .param("count", count.toString())
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden()); // Spring Security retorna 403 para acesso sem auth
  }

  @Test
  void allocateTroops_WithInvalidGameId_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long invalidGameId = 999L;
    Long territoryId = testTerritory.getId();
    Integer count = 3;

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/allocate", invalidGameId)
        .param("territoryId", territoryId.toString())
        .param("count", count.toString())
        .header("Authorization", authToken)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void allocateTroops_WithZeroCount_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long territoryId = testTerritory.getId();
    Integer count = 0;

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/allocate", testGame.getId())
        .param("territoryId", territoryId.toString())
        .param("count", count.toString())
        .header("Authorization", authToken)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void allocateTroops_WithMissingParameters_ShouldReturnBadRequest() throws Exception {
    // Act & Assert - Missing territoryId
    mockMvc.perform(post("/api/games/{gameId}/allocate", testGame.getId())
        .param("count", "3")
        .header("Authorization", authToken)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    // Act & Assert - Missing count
    mockMvc.perform(post("/api/games/{gameId}/allocate", testGame.getId())
        .param("territoryId", testTerritory.getId().toString())
        .header("Authorization", authToken)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void allocateTroops_WithInvalidTerritoryId_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long invalidTerritoryId = 999L;
    Integer count = 3;

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/allocate", testGame.getId())
        .param("territoryId", invalidTerritoryId.toString())
        .param("count", count.toString())
        .header("Authorization", authToken)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}