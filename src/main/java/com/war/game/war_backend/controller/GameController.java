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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

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

    @PostMapping("/join/{lobbyId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Player>> joinLobby(@PathVariable Long lobbyId, Principal principal) {
        String username = principal.getName();
        Player player = playerService.getPlayerByUsername(username);

        Game updatedLobby = gameService.addPlayerToLobby(lobbyId, player);
        
        // Envia a lista atualizada de jogadores para todos que estão no lobby.
        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, updatedLobby.getPlayers());

        return ResponseEntity.ok(updatedLobby.getPlayers());
    }

    @PostMapping("/leave/{lobbyId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Player>> leaveLobby(@PathVariable Long lobbyId, Principal principal) {
        String username = principal.getName();
        Player player = playerService.getPlayerByUsername(username);

        Game updatedLobby = gameService.removePlayerFromLobby(lobbyId, player);

        // Verifica se o lobby ainda existe após o jogador sair
        if (updatedLobby == null) {
            // Envie uma lista vazia ou uma notificação de que o lobby foi excluído
            return ResponseEntity.ok(List.of()); 
        }

        // Envia a lista atualizada de jogadores para todos que estão no lobby.
        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, updatedLobby.getPlayers());

        return ResponseEntity.ok(updatedLobby.getPlayers());
    }
}