package com.war.game.war_backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import com.war.game.war_backend.controller.dto.request.PlayerRegistrationDto;
import com.war.game.war_backend.controller.dto.request.PlayerUpdateDto;
import com.war.game.war_backend.controller.dto.response.PlayerDto;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.security.jwt.JwtTokenUtil;
import com.war.game.war_backend.services.PlayerService;

class PlayerControllerUnitTest {

  @Mock private PlayerService playerService;

  @InjectMocks private PlayerController controller;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    controller =
        new PlayerController(
            playerService, mock(AuthenticationManager.class), mock(JwtTokenUtil.class));
  }

  @Test
  void registerPlayer_returnsCreated_whenServiceSucceeds() {
    PlayerRegistrationDto dto = new PlayerRegistrationDto("usuario1", "u@ex.com", "senha123");
    Player returned = new Player(); // adapte para seu construtor / setters
    returned.setUsername("usuario1");
    returned.setEmail("u@ex.com");

    when(playerService.registerNewPlayer(any(PlayerRegistrationDto.class))).thenReturn(returned);

    ResponseEntity<Player> response = controller.registerPlayer(dto);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertSame(returned, response.getBody());
    verify(playerService, times(1)).registerNewPlayer(any(PlayerRegistrationDto.class));
  }

  @Test
  void registerPlayer_returnsConflict_whenServiceThrowsIllegalStateException() {
    PlayerRegistrationDto dto = new PlayerRegistrationDto("usuario1", "u@ex.com", "senha123");

    when(playerService.registerNewPlayer(any())).thenThrow(new IllegalStateException("existente"));

    ResponseEntity<Player> response = controller.registerPlayer(dto);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertNull(response.getBody());
    verify(playerService, times(1)).registerNewPlayer(any(PlayerRegistrationDto.class));
  }

  @Test
  void listPlayers_returnsOk_withPlayerDtoList() {
    Player player1 = new Player();
    player1.setId(1L);
    player1.setUsername("usuario1");
    player1.setEmail("u1@ex.com");

    Player player2 = new Player();
    player2.setId(2L);
    player2.setUsername("usuario2");
    player2.setEmail("u2@ex.com");

    when(playerService.getAllPlayers()).thenReturn(Arrays.asList(player1, player2));

    ResponseEntity<List<PlayerDto>> response = controller.listPlayers();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());
    assertEquals("usuario1", response.getBody().get(0).getUsername());
    assertEquals("usuario2", response.getBody().get(1).getUsername());
    verify(playerService, times(1)).getAllPlayers();
  }

  @Test
  void getPlayer_returnsOk_whenPlayerExists() {
    Player player = new Player();
    player.setId(1L);
    player.setUsername("usuario1");
    player.setEmail("u1@ex.com");

    when(playerService.getPlayerById(1L)).thenReturn(player);

    ResponseEntity<PlayerDto> response = controller.getPlayer(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("usuario1", response.getBody().getUsername());
    assertEquals("u1@ex.com", response.getBody().getEmail());
    verify(playerService, times(1)).getPlayerById(1L);
  }

  @Test
  void getPlayer_returnsNotFound_whenPlayerNotExists() {
    when(playerService.getPlayerById(1L))
        .thenThrow(new IllegalArgumentException("Jogador não encontrado"));

    ResponseEntity<PlayerDto> response = controller.getPlayer(1L);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(playerService, times(1)).getPlayerById(1L);
  }

  @Test
  void updatePlayer_returnsOk_whenUpdateSucceeds() {
    PlayerUpdateDto updateDto = new PlayerUpdateDto();
    updateDto.setEmail("novo@email.com");
    updateDto.setImageUrl("nova-imagem.jpg");

    Player updatedPlayer = new Player();
    updatedPlayer.setId(1L);
    updatedPlayer.setUsername("usuario1");
    updatedPlayer.setEmail("novo@email.com");
    updatedPlayer.setImageUrl("nova-imagem.jpg");

    when(playerService.updatePlayer(1L, updateDto)).thenReturn(updatedPlayer);

    ResponseEntity<PlayerDto> response = controller.updatePlayer(1L, updateDto);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("novo@email.com", response.getBody().getEmail());
    assertEquals("nova-imagem.jpg", response.getBody().getImageUrl());
    verify(playerService, times(1)).updatePlayer(1L, updateDto);
  }

  @Test
  void updatePlayer_returnsNotFound_whenPlayerNotExists() {
    PlayerUpdateDto updateDto = new PlayerUpdateDto();
    updateDto.setEmail("novo@email.com");

    when(playerService.updatePlayer(1L, updateDto))
        .thenThrow(new IllegalArgumentException("Jogador não encontrado"));

    ResponseEntity<PlayerDto> response = controller.updatePlayer(1L, updateDto);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(playerService, times(1)).updatePlayer(1L, updateDto);
  }
}
