package com.war.game.war_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.GameTerritoryRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.PlayerRepository;
import com.war.game.war_backend.repository.RoleRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(com.war.game.war_backend.config.BaseTestConfiguration.class)
@Transactional
public class AuthControllerIntegrationTest {
    

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameTerritoryRepository gameTerritoryRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerGameRepository playerGameRepository;

    @Autowired
    private PlayerRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        gameTerritoryRepository.deleteAll();
        playerGameRepository.deleteAll();
        gameRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void quandoAutenticarUsuarioComCredenciaisValidas_entaoRetornarOkComJwt() throws Exception {
        String username = "testuser_valid_" + System.currentTimeMillis();
    Player testUser = new Player(username, "test@email.com", passwordEncoder.encode("password123"));
    testUser.setRoles(new HashSet<>());
    userRepository.save(testUser);
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", "password123");

        mockMvc.perform(post("/api/v1/players/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void quandoAutenticarUsuarioComSenhaInvalida_entaoRetornarUnauthorized() throws Exception {
        String username = "testuser_wrongpass_" + System.currentTimeMillis();
    Player testUser = new Player(username, "test@email.com", passwordEncoder.encode("password123"));
    testUser.setRoles(new HashSet<>());
    userRepository.save(testUser);
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", "wrongpassword");

        mockMvc.perform(post("/api/v1/players/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void quandoAutenticarUsuarioInexistente_entaoRetornarUnauthorized() throws Exception {
        String username = "nonexistent_" + UUID.randomUUID();
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", "anypassword");

        mockMvc.perform(post("/api/v1/players/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void quandoAutenticarComUsernameEmBranco_entaoRetornarBadRequest() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "");
        loginRequest.put("password", "password123");

        mockMvc.perform(post("/api/v1/players/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void quandoAcessarEndpointProtegidoSemAutenticacao_entaoRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/api/games/lobbies"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void quandoLogadoComoUser_entaoPodeAcessarEndpointDeUser() throws Exception {
        mockMvc.perform(get("/api/games/lobbies"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void quandoLogadoComoAdmin_entaoPodeAcessarEndpointDeAdmin() throws Exception {
        // Admin também pode acessar lista de lobbies
        mockMvc.perform(get("/api/games/lobbies"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
}
