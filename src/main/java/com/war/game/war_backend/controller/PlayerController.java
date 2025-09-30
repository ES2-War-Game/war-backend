package com.war.game.war_backend.controller;

import com.war.game.war_backend.controller.dto.request.LoginRequestDto;
import com.war.game.war_backend.controller.dto.request.PlayerRegistrationDto;
import com.war.game.war_backend.controller.dto.request.PlayerUpdateDto;
import com.war.game.war_backend.controller.dto.response.JwtResponseDto;
import com.war.game.war_backend.controller.dto.response.PlayerDto;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.security.jwt.JwtTokenUtil;
import com.war.game.war_backend.services.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/players")
@Tag(name = "Jogadores", description = "Endpoints para gerenciamento de jogadores e autenticação")
public class PlayerController {

  private final PlayerService playerService;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenUtil jwtTokenUtil;

  @Autowired
  public PlayerController(PlayerService playerService, AuthenticationManager authenticationManager,
      JwtTokenUtil jwtTokenUtil) {
    this.playerService = playerService;
    this.authenticationManager = authenticationManager;
    this.jwtTokenUtil = jwtTokenUtil;
  }

  @PostMapping("/register")
  @Operation(summary = "Registrar um novo jogador", description = "Cria uma nova conta de jogador no banco de dados.")
  @ApiResponse(responseCode = "201", description = "Jogador registrado com sucesso")
  @ApiResponse(responseCode = "400", description = "Dados de registro inválidos")
  @ApiResponse(responseCode = "409", description = "Nome de usuário ou e-mail já existe")
  public ResponseEntity<Player> registerPlayer(@Valid @RequestBody PlayerRegistrationDto registrationDto) {
    try {
      Player newPlayer = playerService.registerNewPlayer(registrationDto);
      return new ResponseEntity<>(newPlayer, HttpStatus.CREATED);
    } catch (IllegalStateException e) {
      return new ResponseEntity<>(null, HttpStatus.CONFLICT);
    }
  }

  @PostMapping("/login")
  @Operation(summary = "Login do jogador", description = "Autentica um jogador e inicia uma sessão.")
  @ApiResponse(responseCode = "200", description = "Login bem-sucedido")
  @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
  public ResponseEntity<JwtResponseDto> loginPlayer(@RequestBody LoginRequestDto loginDto) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    final String token = jwtTokenUtil.generateToken(userDetails);

    return ResponseEntity.ok(new JwtResponseDto(token));
  }

  @GetMapping
  @Operation(summary = "Listar todos os jogadores", description = "Retorna uma lista de todos os jogadores cadastrados.")
  @ApiResponse(responseCode = "200", description = "Lista de jogadores retornada com sucesso")
  public ResponseEntity<List<PlayerDto>> listPlayers() {
    List<Player> players = playerService.getAllPlayers();
    List<PlayerDto> dtos = players.stream()
        .map(p -> new PlayerDto(p.getId(), p.getUsername(), p.getEmail(), p.getImageUrl()))
        .toList();
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar jogador por ID", description = "Retorna os dados de um jogador específico.")
  @ApiResponse(responseCode = "200", description = "Jogador encontrado")
  @ApiResponse(responseCode = "404", description = "Jogador não encontrado")
  public ResponseEntity<PlayerDto> getPlayer(@PathVariable Long id) {
    try {
      Player player = playerService.getPlayerById(id);
      PlayerDto dto = new PlayerDto(
          player.getId(), player.getUsername(), player.getEmail(), player.getImageUrl());
      return ResponseEntity.ok(dto);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Atualizar dados do jogador", description = "Atualiza informações do jogador pelo ID.")
  @ApiResponse(responseCode = "200", description = "Jogador atualizado com sucesso")
  @ApiResponse(responseCode = "404", description = "Jogador não encontrado")
  public ResponseEntity<PlayerDto> updatePlayer(@PathVariable Long id,
      @RequestBody PlayerUpdateDto updateDto) {
    try {
      Player updated = playerService.updatePlayer(id, updateDto);
      PlayerDto dto = new PlayerDto(
          updated.getId(), updated.getUsername(), updated.getEmail(), updated.getImageUrl());
      return ResponseEntity.ok(dto);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }
}