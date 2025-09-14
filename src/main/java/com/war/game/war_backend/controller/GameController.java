package com.war.game.war_backend.controller;

import com.war.game.war_backend.controller.dto.request.LobbyCreationRequestDto;
import com.war.game.war_backend.controller.dto.response.LobbyCreationResponseDto;
import com.war.game.war_backend.controller.dto.response.LobbyListResponseDto;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.services.GameService;
import com.war.game.war_backend.services.PlayerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final PlayerService playerService;

    @PostMapping("/create-lobby")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LobbyCreationResponseDto> createLobby(@Valid @RequestBody LobbyCreationRequestDto request, Principal principal) {
        // Obtém o nome de usuário do token JWT
        String username = principal.getName();

        Player creator = playerService.getPlayerByUsername(username);

        Game newGame = gameService.createNewLobby(request.getLobbyName(), creator);

        LobbyCreationResponseDto response = new LobbyCreationResponseDto(newGame.getId(), newGame.getName());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/lobbies")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LobbyListResponseDto>> getAllLobbies() {
        List<Game> lobbies = gameService.findAllLobbies();

        List<LobbyListResponseDto> lobbyDtos = lobbies.stream()
            .map(lobby -> new LobbyListResponseDto(
                lobby.getId(),
                lobby.getName(),
                lobby.getStatus()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(lobbyDtos);
    }
}