package com.war.game.war_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.war.game.war_backend.controller.dto.response.GameStateResponseDto;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.services.GameService;
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

import java.security.Principal;

@ExtendWith(MockitoExtension.class)
class GameControllerAllocateTroopsTest {

  @Mock
  private GameService gameService;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @Mock
  private Principal principal;

  @InjectMocks
  private GameController gameController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(gameController)
        .setControllerAdvice() // Adiciona suporte para @ControllerAdvice
        .build();
    objectMapper = new ObjectMapper();
  }

  @Test
  void allocateTroops_Success_ShouldReturnUpdatedGame() throws Exception {
    // Arrange
    Long gameId = 1L;
    Long territoryId = 5L;
    Integer count = 3;
    String username = "testuser";

    Game mockGame = new Game();
    mockGame.setId(gameId);
    mockGame.setName("Test Game");
    mockGame.setStatus("In Game - Initial Allocation");
    mockGame.setPlayerGames(new java.util.HashSet<>()); // Inicializar coleções para evitar NPE
    mockGame.setGameTerritories(new java.util.HashSet<>());

    when(principal.getName()).thenReturn(username);
    when(gameService.allocateTroops(gameId, username, territoryId, count)).thenReturn(mockGame);

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/allocate", gameId)
        .param("territoryId", territoryId.toString())
        .param("count", count.toString())
        .principal(principal)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(gameId))
        .andExpect(jsonPath("$.name").value("Test Game"))
        .andExpect(jsonPath("$.status").value("In Game - Initial Allocation"));

    // Verify interactions
    verify(gameService, times(1)).allocateTroops(gameId, username, territoryId, count);
    // Verifica que um GameStateResponseDto foi enviado via WebSocket
    verify(messagingTemplate, times(1)).convertAndSend(
        eq("/topic/game/" + gameId + "/state"), 
        any(GameStateResponseDto.class)
    );
  }

  @Test
  void allocateTroops_InvalidInput_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long gameId = 1L;
    Long territoryId = 5L;
    Integer count = 3;
    String username = "testuser";
    String errorMessage = "Tropas insuficientes na reserva";

    when(principal.getName()).thenReturn(username);
    when(gameService.allocateTroops(gameId, username, territoryId, count))
        .thenThrow(new RuntimeException(errorMessage));

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/allocate", gameId)
        .param("territoryId", territoryId.toString())
        .param("count", count.toString())
        .principal(principal)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(errorMessage));

    // Verify interactions
    verify(gameService, times(1)).allocateTroops(gameId, username, territoryId, count);
    verify(messagingTemplate, times(0)).convertAndSend(any(String.class), any(Object.class));
  }

  @Test
  void allocateTroops_NegativeCount_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long gameId = 1L;
    Long territoryId = 5L;
    Integer count = -1;
    String username = "testuser";
    String errorMessage = "Número de tropas deve ser positivo";

    when(principal.getName()).thenReturn(username);
    when(gameService.allocateTroops(gameId, username, territoryId, count))
        .thenThrow(new RuntimeException(errorMessage));

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/allocate", gameId)
        .param("territoryId", territoryId.toString())
        .param("count", count.toString())
        .principal(principal)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(errorMessage));
  }

  @Test
  void allocateTroops_GameNotFound_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long gameId = 999L;
    Long territoryId = 5L;
    Integer count = 3;
    String username = "testuser";
    String errorMessage = "Jogo não encontrado";

    when(principal.getName()).thenReturn(username);
    when(gameService.allocateTroops(gameId, username, territoryId, count))
        .thenThrow(new RuntimeException(errorMessage));

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/allocate", gameId)
        .param("territoryId", territoryId.toString())
        .param("count", count.toString())
        .principal(principal)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(errorMessage));
  }

  @Test
  void allocateTroops_TerritoryNotOwned_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long gameId = 1L;
    Long territoryId = 5L;
    Integer count = 3;
    String username = "testuser";
    String errorMessage = "Território não pertence ao jogador";

    when(principal.getName()).thenReturn(username);
    when(gameService.allocateTroops(gameId, username, territoryId, count))
        .thenThrow(new RuntimeException(errorMessage));

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/allocate", gameId)
        .param("territoryId", territoryId.toString())
        .param("count", count.toString())
        .principal(principal)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(errorMessage));
  }

  @Test
  void allocateTroops_NotPlayerTurn_ShouldReturnBadRequest() throws Exception {
    // Arrange
    Long gameId = 1L;
    Long territoryId = 5L;
    Integer count = 3;
    String username = "testuser";
    String errorMessage = "Não é o turno do jogador";

    when(principal.getName()).thenReturn(username);
    when(gameService.allocateTroops(gameId, username, territoryId, count))
        .thenThrow(new RuntimeException(errorMessage));

    // Act & Assert
    mockMvc.perform(post("/api/games/{gameId}/allocate", gameId)
        .param("territoryId", territoryId.toString())
        .param("count", count.toString())
        .principal(principal)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(errorMessage));
  }
}