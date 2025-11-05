package com.war.game.war_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.war.game.war_backend.controller.dto.request.LoginRequestDto;
import com.war.game.war_backend.controller.dto.request.PlayerRegistrationDto;
import com.war.game.war_backend.controller.dto.request.PlayerUpdateDto;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.security.jwt.JwtTokenUtil;
import com.war.game.war_backend.services.PlayerService;

@ExtendWith(MockitoExtension.class)
class PlayerControllerWebTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private PlayerService playerService;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtTokenUtil jwtTokenUtil;

  private static final String REGISTER_ENDPOINT = "/api/v1/players/register";
  private static final String LOGIN_ENDPOINT = "/api/v1/players/login";
  private static final String PLAYERS_ENDPOINT = "/api/v1/players";

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);

    PlayerController controller =
        new PlayerController(playerService, authentication_manager(), jwtTokenUtil);

    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).setValidator(validator).build();
  }

  private AuthenticationManager authentication_manager() {
    return authenticationManager;
  }

  @Test
  void postRegister_returns201_andJsonBody_whenValid() throws Exception {
    PlayerRegistrationDto dto = new PlayerRegistrationDto("usuarioValido", "u@ex.com", "senha123");

    Player savedPlayer = new Player();
    savedPlayer.setId(1L);
    savedPlayer.setUsername("usuarioValido");
    savedPlayer.setEmail("u@ex.com");

    when(playerService.registerNewPlayer(any(PlayerRegistrationDto.class))).thenReturn(savedPlayer);

    mockMvc
        .perform(
            post(REGISTER_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.username").value("usuarioValido"))
        .andExpect(jsonPath("$.email").value("u@ex.com"));
  }

  @Test
  void postRegister_returns400_whenInvalidPayload() throws Exception {
    PlayerRegistrationDto invalidDto = new PlayerRegistrationDto("ab", "not-an-email", "123");

    mockMvc
        .perform(
            post(REGISTER_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void postRegister_returns409_whenServiceThrowsConflict() throws Exception {
    PlayerRegistrationDto dto = new PlayerRegistrationDto("usuario", "u@ex.com", "senha123");

    when(playerService.registerNewPlayer(any(PlayerRegistrationDto.class)))
        .thenThrow(new IllegalStateException("exists"));

    mockMvc
        .perform(
            post(REGISTER_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isConflict());
  }

  @Test
  void getPlayers_returns200_andJsonArray_whenServiceReturnsPlayers() throws Exception {
    Player player1 = new Player();
    player1.setId(1L);
    player1.setUsername("usuario1");
    player1.setEmail("u1@ex.com");

    Player player2 = new Player();
    player2.setId(2L);
    player2.setUsername("usuario2");
    player2.setEmail("u2@ex.com");

    when(playerService.getAllPlayers()).thenReturn(Arrays.asList(player1, player2));

    mockMvc
        .perform(get(PLAYERS_ENDPOINT))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].username").value("usuario1"))
        .andExpect(jsonPath("$[0].email").value("u1@ex.com"))
        .andExpect(jsonPath("$[1].username").value("usuario2"))
        .andExpect(jsonPath("$[1].email").value("u2@ex.com"));
  }

  @Test
  void getPlayer_returns200_andJsonBody_whenPlayerExists() throws Exception {
    Player player = new Player();
    player.setId(1L);
    player.setUsername("usuario1");
    player.setEmail("u1@ex.com");

    when(playerService.getPlayerById(1L)).thenReturn(player);

    mockMvc
        .perform(get(PLAYERS_ENDPOINT + "/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.username").value("usuario1"))
        .andExpect(jsonPath("$.email").value("u1@ex.com"));
  }

  @Test
  void login_returns200_andToken_whenCredentialsValid() throws Exception {
    LoginRequestDto loginDto = new LoginRequestDto("usuario", "senha123");
    // Mock do UserDetails
    User userDetails = new User("usuario", "senha123", Collections.emptyList());

    // Mock do Authentication que retorna o UserDetails
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());

    when(authenticationManager.authenticate(any())).thenReturn(authToken);
    when(jwtTokenUtil.generateToken(any())).thenReturn("jwt-token");

    Player savedPlayer = new Player();
    savedPlayer.setId(42L);
    savedPlayer.setUsername("usuario");
    savedPlayer.setEmail("usuario@example.com");
    when(playerService.getPlayerByUsername("usuario")).thenReturn(savedPlayer);

    mockMvc
        .perform(
            post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.token").value("jwt-token"));
  }

  @Test
  void login_returns401_whenCredentialsInvalid() throws Exception {
    LoginRequestDto loginDto = new LoginRequestDto("usuario", "senha-errada");
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Invalid credentials"));

    mockMvc
        .perform(
            post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_returns400_whenRequestInvalid() throws Exception {
    LoginRequestDto loginDto = new LoginRequestDto("", "");

    mockMvc
        .perform(
            post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getPlayer_returns404_whenPlayerNotFound() throws Exception {
    when(playerService.getPlayerById(1L))
        .thenThrow(new IllegalArgumentException("Jogador não encontrado"));

    mockMvc.perform(get(PLAYERS_ENDPOINT + "/1")).andExpect(status().isNotFound());
  }

  @Test
  void patchPlayer_returns200_andUpdatedJson_whenValidUpdate() throws Exception {
    PlayerUpdateDto updateDto = new PlayerUpdateDto();
    updateDto.setEmail("novo@email.com");
    updateDto.setImageUrl("nova-imagem.jpg");

    Player updatedPlayer = new Player();
    updatedPlayer.setId(1L);
    updatedPlayer.setUsername("usuario1");
    updatedPlayer.setEmail("novo@email.com");
    updatedPlayer.setImageUrl("nova-imagem.jpg");

    when(playerService.updatePlayer(eq(1L), any(PlayerUpdateDto.class))).thenReturn(updatedPlayer);

    mockMvc
        .perform(
            patch(PLAYERS_ENDPOINT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value("novo@email.com"))
        .andExpect(jsonPath("$.imageUrl").value("nova-imagem.jpg"));
  }

  @Test
  void patchPlayer_returns404_whenPlayerNotFound() throws Exception {
    PlayerUpdateDto updateDto = new PlayerUpdateDto();
    updateDto.setEmail("novo@email.com");

    when(playerService.updatePlayer(eq(1L), any(PlayerUpdateDto.class)))
        .thenThrow(new IllegalArgumentException("Jogador não encontrado"));

    mockMvc
        .perform(
            patch(PLAYERS_ENDPOINT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound());
  }
}
