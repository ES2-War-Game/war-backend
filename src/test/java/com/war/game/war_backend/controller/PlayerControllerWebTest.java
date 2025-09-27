package com.war.game.war_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.war.game.war_backend.controller.dto.request.PlayerRegistrationDto;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.security.jwt.JwtTokenUtil;
import com.war.game.war_backend.services.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class PlayerControllerWebTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private PlayerService playerService;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtTokenUtil jwtTokenUtil;

  private static final String REGISTER_ENDPOINT = "/api/v1/players/register";

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);

    PlayerController controller = new PlayerController(playerService, authentication_manager(), jwtTokenUtil);

    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setValidator(validator)
        .build();
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

    mockMvc.perform(post(REGISTER_ENDPOINT)
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

    mockMvc.perform(post(REGISTER_ENDPOINT)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void postRegister_returns409_whenServiceThrowsConflict() throws Exception {
    PlayerRegistrationDto dto = new PlayerRegistrationDto("usuario", "u@ex.com", "senha123");

    when(playerService.registerNewPlayer(any(PlayerRegistrationDto.class)))
        .thenThrow(new IllegalStateException("exists"));

    mockMvc.perform(post(REGISTER_ENDPOINT)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isConflict());
  }
}
