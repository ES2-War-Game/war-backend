package com.war.game.war_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.war.game.war_backend.config.BaseTestConfiguration;
import com.war.game.war_backend.controller.dto.request.AttackRequestDto;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.model.TerritoryBorder;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.GameTerritoryRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.PlayerRepository;
import com.war.game.war_backend.repository.TerritoryBorderRepository;
import com.war.game.war_backend.repository.TerritoryRepository;
import com.war.game.war_backend.security.jwt.JwtTokenUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(BaseTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class GameControllerAttackIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private GameRepository gameRepository;

  @Autowired private PlayerRepository playerRepository;

  @Autowired private TerritoryRepository territoryRepository;

  @Autowired private PlayerGameRepository playerGameRepository;

  @Autowired private GameTerritoryRepository gameTerritoryRepository;

  @Autowired private TerritoryBorderRepository territoryBorderRepository;

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private ObjectMapper objectMapper;

  private Player attacker;
  private Player defender;
  private Game testGame;
  private Territory sourceTerritory;
  private Territory targetTerritory;
  private PlayerGame attackerPlayerGame;
  private PlayerGame defenderPlayerGame;
  private GameTerritory sourceGameTerritory;
  private GameTerritory targetGameTerritory;
  private String attackerAuthToken;

  @BeforeEach
  void setUp() {
    // Criar jogadores de teste
    attacker = new Player();
    attacker.setUsername("attacker");
    attacker.setEmail("attacker@example.com");
    attacker.setPassword(passwordEncoder.encode("password"));
    attacker.setRoles(new java.util.HashSet<>());
    attacker = playerRepository.save(attacker);

    defender = new Player();
    defender.setUsername("defender");
    defender.setEmail("defender@example.com");
    defender.setPassword(passwordEncoder.encode("password"));
    defender.setRoles(new java.util.HashSet<>());
    defender = playerRepository.save(defender);

    // Buscar territórios existentes criados pelo TerritoryInitializer
    sourceTerritory =
        territoryRepository
            .findByName("BRASIL")
            .orElseThrow(() -> new RuntimeException("Território BRASIL não foi inicializado"));

    targetTerritory =
        territoryRepository
            .findByName("ARGENTINA")
            .orElseThrow(() -> new RuntimeException("Território ARGENTINA não foi inicializado"));

    // Verificar se existe fronteira entre os territórios
    territoryBorderRepository
        .findByTerritoryIds(sourceTerritory.getId(), targetTerritory.getId())
        .orElseGet(
            () -> {
              // Se não existir, criar uma fronteira para o teste
              var newBorder = new TerritoryBorder();
              newBorder.setTerritoryA(sourceTerritory);
              newBorder.setTerritoryB(targetTerritory);
              return territoryBorderRepository.save(newBorder);
            });

    // Criar jogo de teste
    testGame = new Game();
    testGame.setName("Test Attack Game");
    testGame.setStatus("ATTACK"); // Use exatamente o nome da constante do enum GameStatus
    testGame.setCreatedAt(java.time.LocalDateTime.now());
    testGame = gameRepository.save(testGame);

    // Criar PlayerGame para atacante (jogador da vez)
    attackerPlayerGame = new PlayerGame();
    attackerPlayerGame.setGame(testGame);
    attackerPlayerGame.setPlayer(attacker);
    attackerPlayerGame.setUsername(attacker.getUsername());
    attackerPlayerGame.setTurnOrder(1);
    attackerPlayerGame.setUnallocatedArmies(0);
    attackerPlayerGame.setConqueredTerritoryThisTurn(false);
    attackerPlayerGame.setStillInGame(true);
    attackerPlayerGame = playerGameRepository.save(attackerPlayerGame);

    // Criar PlayerGame para defensor
    defenderPlayerGame = new PlayerGame();
    defenderPlayerGame.setGame(testGame);
    defenderPlayerGame.setPlayer(defender);
    defenderPlayerGame.setUsername(defender.getUsername());
    defenderPlayerGame.setTurnOrder(2);
    defenderPlayerGame.setUnallocatedArmies(0);
    defenderPlayerGame.setConqueredTerritoryThisTurn(false);
    defenderPlayerGame.setStillInGame(true);
    defenderPlayerGame = playerGameRepository.save(defenderPlayerGame);

    // Configurar o jogo para ter o atacante como jogador da vez
    testGame.setTurnPlayer(attackerPlayerGame);
    testGame = gameRepository.save(testGame);

    // Recarregar o jogo do banco para ter as associações atualizadas
    testGame = gameRepository.findById(testGame.getId()).orElseThrow();

    // Criar GameTerritory para o território de origem (pertence ao atacante)
    sourceGameTerritory = new GameTerritory();
    sourceGameTerritory.setGame(testGame);
    sourceGameTerritory.setTerritory(sourceTerritory);
    sourceGameTerritory.setOwner(attackerPlayerGame);
    sourceGameTerritory.setStaticArmies(10); // 10 tropas estáticas no território do atacante
    sourceGameTerritory.setMovedInArmies(0); // Nenhuma tropa movida no território do atacante
    sourceGameTerritory = gameTerritoryRepository.save(sourceGameTerritory);

    // Criar GameTerritory para o território alvo (pertence ao defensor)
    targetGameTerritory = new GameTerritory();
    targetGameTerritory.setGame(testGame);
    targetGameTerritory.setTerritory(targetTerritory);
    targetGameTerritory.setOwner(defenderPlayerGame);
    targetGameTerritory.setStaticArmies(3); // 3 tropas estáticas no território do defensor
    targetGameTerritory.setMovedInArmies(0); // Nenhuma tropa movida no território do defensor
    targetGameTerritory = gameTerritoryRepository.save(targetGameTerritory);

    // Gerar token JWT para o atacante
    org.springframework.security.core.userdetails.UserDetails attackerUserDetails =
        org.springframework.security.core.userdetails.User.withUsername(attacker.getUsername())
            .password(attacker.getPassword())
            .authorities("USER")
            .build();
    attackerAuthToken = "Bearer " + jwtTokenUtil.generateToken(attackerUserDetails);
  }

  @Test
  void attackTerritory_WithValidData_ShouldReturnSuccess() throws Exception {
    // Arrange
    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(sourceGameTerritory.getId());
    attackRequest.setTargetTerritoryId(targetGameTerritory.getId());
    attackRequest.setAttackDiceCount(3);
    attackRequest.setTroopsToMoveAfterConquest(3);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/games/{gameId}/attack", testGame.getId())
                .header("Authorization", attackerAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attackRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testGame.getId()))
        .andExpect(jsonPath("$.status").value("ATTACK"));
  }

  @Test
  void attackTerritory_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
    // Arrange
    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(sourceGameTerritory.getId());
    attackRequest.setTargetTerritoryId(targetGameTerritory.getId());
    attackRequest.setAttackDiceCount(3);
    attackRequest.setTroopsToMoveAfterConquest(3);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/games/{gameId}/attack", testGame.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attackRequest)))
        .andExpect(status().isUnauthorized()); // 401 quando não há autenticação
  }

  @Test
  void attackTerritory_WithInvalidGamePhase_ShouldReturnBadRequest() throws Exception {
    // Arrange
    testGame.setStatus("In Game - Reinforcement");
    gameRepository.save(testGame);

    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(sourceGameTerritory.getId());
    attackRequest.setTargetTerritoryId(targetGameTerritory.getId());
    attackRequest.setAttackDiceCount(3);
    attackRequest.setTroopsToMoveAfterConquest(3);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/games/{gameId}/attack", testGame.getId())
                .header("Authorization", attackerAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attackRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void attackTerritory_WithNotPlayerTurn_ShouldReturnBadRequest() throws Exception {
    // Arrange
    testGame.setTurnPlayer(defenderPlayerGame);
    gameRepository.save(testGame);

    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(sourceGameTerritory.getId());
    attackRequest.setTargetTerritoryId(targetGameTerritory.getId());
    attackRequest.setAttackDiceCount(3);
    attackRequest.setTroopsToMoveAfterConquest(3);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/games/{gameId}/attack", testGame.getId())
                .header("Authorization", attackerAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attackRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void attackTerritory_WithAttackingOwnTerritory_ShouldReturnBadRequest() throws Exception {
    // Arrange - Colocar o território alvo como pertencente ao atacante
    targetGameTerritory.setOwner(attackerPlayerGame);
    gameTerritoryRepository.save(targetGameTerritory);

    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(sourceGameTerritory.getId());
    attackRequest.setTargetTerritoryId(targetGameTerritory.getId());
    attackRequest.setAttackDiceCount(3);
    attackRequest.setTroopsToMoveAfterConquest(3);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/games/{gameId}/attack", testGame.getId())
                .header("Authorization", attackerAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attackRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void attackTerritory_WithInsufficientArmies_ShouldReturnBadRequest() throws Exception {
    // Arrange - Reduzir tropas no território de origem
    sourceGameTerritory.setStaticArmies(3);
    sourceGameTerritory.setMovedInArmies(0);
    gameTerritoryRepository.save(sourceGameTerritory);

    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(sourceGameTerritory.getId());
    attackRequest.setTargetTerritoryId(targetGameTerritory.getId());
    attackRequest.setAttackDiceCount(3); // Máximo seria 2 (3 tropas - 1 que deve ficar)
    attackRequest.setTroopsToMoveAfterConquest(3);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/games/{gameId}/attack", testGame.getId())
                .header("Authorization", attackerAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attackRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void attackTerritory_WithInvalidGameId_ShouldReturnBadRequest() throws Exception {
    // Arrange
    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(sourceGameTerritory.getId());
    attackRequest.setTargetTerritoryId(targetGameTerritory.getId());
    attackRequest.setAttackDiceCount(3);
    attackRequest.setTroopsToMoveAfterConquest(3);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/games/{gameId}/attack", 999L)
                .header("Authorization", attackerAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attackRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void attackTerritory_WithInvalidTerritoryId_ShouldReturnBadRequest() throws Exception {
    // Arrange
    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(999L); // ID inválido
    attackRequest.setTargetTerritoryId(targetGameTerritory.getId());
    attackRequest.setAttackDiceCount(3);
    attackRequest.setTroopsToMoveAfterConquest(3);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/games/{gameId}/attack", testGame.getId())
                .header("Authorization", attackerAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attackRequest)))
        .andExpect(status().isBadRequest());
  }
}
