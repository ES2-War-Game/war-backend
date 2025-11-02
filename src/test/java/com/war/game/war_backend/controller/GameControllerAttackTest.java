package com.war.game.war_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.war.game.war_backend.controller.dto.request.AttackRequestDto;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.services.GameService;
import com.war.game.war_backend.services.PlayerService;

class GameControllerAttackTest {

  private MockMvc mockMvc;

  @Mock
  private GameService gameService;

  @Mock
  private PlayerService playerService;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @Mock
  private Principal principal;

  @InjectMocks
  private GameController gameController;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(gameController)
        .setControllerAdvice()
        .build();
    objectMapper = new ObjectMapper();

    when(principal.getName()).thenReturn("testuser");
  }

  @Test
  void attackTerritory_WithValidData_ShouldReturnSuccess() throws Exception {
    // Arrange
    Long gameId = 1L;
    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(10L);
    attackRequest.setTargetTerritoryId(11L);
    attackRequest.setAttackDiceCount(3);

    Game mockGame = new Game();
    mockGame.setId(gameId);
    mockGame.setName("Test Game");
    mockGame.setStatus("In Game - Attack");
    mockGame.setCreatedAt(LocalDateTime.now());
    mockGame.setCardSetExchangeCount(0);
    mockGame.setPlayerGames(new HashSet<>());
    mockGame.setGameTerritories(new HashSet<>());

    when(gameService.attackTerritory(anyLong(), anyString(), any(AttackRequestDto.class)))
        .thenReturn(mockGame);

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/attack", gameId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(attackRequest))
        .principal(principal))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(gameId))
        .andExpect(jsonPath("$.status").value("In Game - Attack"));
  }

  @Test
  void attackTerritory_WithInvalidGamePhase_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long gameId = 1L;
    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(10L);
    attackRequest.setTargetTerritoryId(11L);
    attackRequest.setAttackDiceCount(3);

    when(gameService.attackTerritory(anyLong(), anyString(), any(AttackRequestDto.class)))
        .thenThrow(new RuntimeException("Ação inválida. A partida não está na fase de Ataque."));

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/attack", gameId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(attackRequest))
        .principal(principal))
        .andExpect(status().isBadRequest());
  }

  @Test
  void attackTerritory_WithNotPlayerTurn_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long gameId = 1L;
    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(10L);
    attackRequest.setTargetTerritoryId(11L);
    attackRequest.setAttackDiceCount(3);

    when(gameService.attackTerritory(anyLong(), anyString(), any(AttackRequestDto.class)))
        .thenThrow(new RuntimeException("Não é o seu turno para atacar."));

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/attack", gameId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(attackRequest))
        .principal(principal))
        .andExpect(status().isBadRequest());
  }

  @Test
  void attackTerritory_WithNonAdjacentTerritories_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long gameId = 1L;
    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(10L);
    attackRequest.setTargetTerritoryId(11L);
    attackRequest.setAttackDiceCount(3);

    when(gameService.attackTerritory(anyLong(), anyString(), any(AttackRequestDto.class)))
        .thenThrow(new RuntimeException("O território BRASIL não é vizinho do território atacante."));

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/attack", gameId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(attackRequest))
        .principal(principal))
        .andExpect(status().isBadRequest());
  }

  @Test
  void attackTerritory_WithAttackingOwnTerritory_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long gameId = 1L;
    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(10L);
    attackRequest.setTargetTerritoryId(11L);
    attackRequest.setAttackDiceCount(3);

    when(gameService.attackTerritory(anyLong(), anyString(), any(AttackRequestDto.class)))
        .thenThrow(new RuntimeException("Você não pode atacar seu próprio território."));

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/attack", gameId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(attackRequest))
        .principal(principal))
        .andExpect(status().isBadRequest());
  }

  @Test
  void attackTerritory_WithInsufficientArmies_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long gameId = 1L;
    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(10L);
    attackRequest.setTargetTerritoryId(11L);
    attackRequest.setAttackDiceCount(3);

    when(gameService.attackTerritory(anyLong(), anyString(), any(AttackRequestDto.class)))
        .thenThrow(new RuntimeException(
            "Você deve deixar pelo menos um exército no território atacante. Máximo de dados de ataque permitido: 2"));

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/attack", gameId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(attackRequest))
        .principal(principal))
        .andExpect(status().isBadRequest());
  }

  @Test
  void attackTerritory_WithTerritoryNotFound_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long gameId = 1L;
    AttackRequestDto attackRequest = new AttackRequestDto();
    attackRequest.setSourceTerritoryId(999L);
    attackRequest.setTargetTerritoryId(11L);
    attackRequest.setAttackDiceCount(3);

    when(gameService.attackTerritory(anyLong(), anyString(), any(AttackRequestDto.class)))
        .thenThrow(new RuntimeException("Território atacante não encontrado."));

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/attack", gameId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(attackRequest))
        .principal(principal))
        .andExpect(status().isBadRequest());
  }
}
