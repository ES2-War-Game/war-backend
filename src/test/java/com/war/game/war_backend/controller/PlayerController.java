package com.war.game.war_backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.war.game.war_backend.controller.dto.request.PlayerRegistrationDto;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.security.jwt.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import com.war.game.war_backend.services.PlayerService;

class PlayerControllerUnitTest {

  @Mock
  private PlayerService playerService;

  @InjectMocks
  private PlayerController controller;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    controller = new PlayerController(playerService, mock(AuthenticationManager.class), mock(JwtTokenUtil.class));
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
}
