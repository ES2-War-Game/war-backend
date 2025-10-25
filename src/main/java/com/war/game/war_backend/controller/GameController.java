package com.war.game.war_backend.controller;

import com.war.game.war_backend.controller.dto.request.AttackRequestDto;
import com.war.game.war_backend.controller.dto.request.LobbyCreationRequestDto;
import com.war.game.war_backend.controller.dto.response.LobbyCreationResponseDto;
import com.war.game.war_backend.controller.dto.response.LobbyListResponseDto;
import com.war.game.war_backend.controller.dto.response.PlayerLobbyDtoResponse;
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

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Tag(name = "Partida (Game)", description = "Endpoints para gerenciamento de lobbies, início e ciclo de turnos de partidas do War.")
public class GameController {

    private final GameService gameService;
    private final PlayerService playerService;
    private final SimpMessagingTemplate messagingTemplate;

    // --- LOBBY MANAGEMENT ---

    @PostMapping("/create-lobby")
    @Operation(summary = "Cria um novo lobby.", description = "O jogador autenticado torna-se automaticamente o dono do lobby.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LobbyCreationResponseDto> createLobby(@Valid @org.springframework.web.bind.annotation.RequestBody LobbyCreationRequestDto request, Principal principal) {
        // Obtém o nome de usuário do token JWT
        String username = principal.getName();
        Player creator = playerService.getPlayerByUsername(username);

        Game newGame = gameService.createNewLobby(request.getLobbyName(), creator);

        LobbyCreationResponseDto response = new LobbyCreationResponseDto(newGame.getId(), newGame.getName());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/lobbies")
    @Operation(summary = "Lista todos os lobbies (partidas) disponíveis.", description = "Retorna apenas jogos no status 'Lobby'.")
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
    @Operation(summary = "Entra em um lobby existente.", description = "Adiciona o jogador autenticado ao lobby especificado. Envia notificação WebSocket.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlayerLobbyDtoResponse>> joinLobby(
            @Parameter(description = "ID da partida (lobby) que o jogador deseja entrar.") 
            @PathVariable Long lobbyId, 
            Principal principal) {
        String username = principal.getName();
        Player player = playerService.getPlayerByUsername(username);

        Game updatedLobby = gameService.addPlayerToLobby(lobbyId, player);
        
        List<PlayerLobbyDtoResponse> playerDtos = updatedLobby.getPlayerGames().stream()
            .map(playerGame -> new PlayerLobbyDtoResponse(
                playerGame.getId(),
                playerGame.getPlayer().getUsername(),
                playerGame.getColor(),
                playerGame.getIsOwner(),
                playerGame.getIsReady(),
                playerGame.getPlayer().getImageUrl()
            ))
            .collect(Collectors.toList());

        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, playerDtos);

        return ResponseEntity.ok(playerDtos);
    }

    @PostMapping("/leave/{lobbyId}")
    @Operation(summary = "Sai de um lobby.", description = "Remove o jogador autenticado do lobby. Se o dono sair, a posse é transferida ou o lobby é excluído.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlayerLobbyDtoResponse>> leaveLobby(
            @Parameter(description = "ID da partida (lobby) que o jogador deseja sair.") 
            @PathVariable Long lobbyId, 
            Principal principal) {
        String username = principal.getName();
        Player player = playerService.getPlayerByUsername(username);

        Game updatedLobby = gameService.removePlayerFromLobby(lobbyId, player);

        if (updatedLobby == null) {
            messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, List.of());
            return ResponseEntity.ok(List.of()); 
        }

        List<PlayerLobbyDtoResponse> playerDtos = updatedLobby.getPlayerGames().stream()
            .map(playerGame -> new PlayerLobbyDtoResponse(
                playerGame.getId(), 
                playerGame.getPlayer().getUsername(), 
                playerGame.getColor(),
                playerGame.getIsOwner(),
                playerGame.getIsReady(),
                playerGame.getPlayer().getImageUrl()
            ))
            .collect(Collectors.toList());

        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, playerDtos);

        return ResponseEntity.ok(playerDtos);
    }
    
    // --- GAMEPLAY MANAGEMENT ---

    @PostMapping("/start/{lobbyId}")
    @Operation(summary = "Inicia a partida.", 
               description = "Distribui territórios, objetivos e tropas iniciais. Move o jogo de 'Lobby' para 'In Game - Initial Allocation'. Apenas o dono pode chamar.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> startGame(
            @Parameter(description = "ID do lobby a ser iniciado.") 
            @PathVariable Long lobbyId, 
            Principal principal) {
        String username = principal.getName();

        try {
            Game startedGame = gameService.startGame(lobbyId, username);
            messagingTemplate.convertAndSend("/topic/game/" + lobbyId + "/state", startedGame);
            return ResponseEntity.ok(startedGame);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{gameId}/allocate")
    @Operation(summary = "Alocação de Tropas.", 
               description = "Usado tanto na 'Alocação Inicial' (fase: Initial Allocation) quanto no 'Reforço' (fase: Reinforcement). Coloca 'count' tropas no território 'territoryId'. Avança automaticamente a fase/turno quando a reserva zera.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> allocateTroops(
            @Parameter(description = "ID da partida.") @PathVariable Long gameId, 
            @Parameter(description = "ID do território onde as tropas serão colocadas.") @RequestParam Long territoryId,
            @Parameter(description = "Número de tropas a serem alocadas (deve ser <= reserva).") @RequestParam Integer count,
            Principal principal) {
        String username = principal.getName();
        
        try {
            Game updatedGame = gameService.allocateTroops(gameId, username, territoryId, count);
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", updatedGame);
            return ResponseEntity.ok(updatedGame);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{gameId}/end-turn")
    @Operation(summary = "Encerra a fase atual e avança o turno/fase.", 
               description = "Usado para: 1) Mudar de Reinforcement para Attack. 2) Mudar de Movement para o próximo jogador (que começará em Reinforcement).")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> endTurn(
            @Parameter(description = "ID da partida.") 
            @PathVariable Long gameId, 
            Principal principal) {
        String username = principal.getName();
        
        try {
            Game updatedGame = gameService.startNextTurn(gameId, username);

            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", updatedGame);

            return ResponseEntity.ok(updatedGame);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{gameId}/trade-cards")
    @Operation(
        summary = "Troca um conjunto de 3 cartas por tropas de reforço.", 
        description = "Pode ser chamado múltiplas vezes na fase de Reinforcement, desde que o jogador tenha pelo menos 3 cartas na mão."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> tradeCards(
        @Parameter(description = "ID da partida.") 
        @PathVariable Long gameId, 
        
        @RequestBody(
            description = "Lista de IDs das entidades PlayerCard (posse) que o jogador deseja trocar. Deve conter exatamente 3 IDs.",
            required = true,
            content = @Content(
                schema = @Schema(implementation = List.class, type = "array", example = "[101, 102, 103]")
            )
        )
        @org.springframework.web.bind.annotation.RequestBody List<Long> playerCardIds,
        Principal principal
    ) {
        String username = principal.getName();
        
        try {
            Game updatedGame = gameService.tradeCardsForReinforcements(gameId, username, playerCardIds);

            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", updatedGame);
            return ResponseEntity.ok(updatedGame);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{gameId}/attack")
    @Operation(summary = "Inicia um ataque entre dois territórios.", 
            description = "Realiza a rolagem de dados, aplica perdas e, se o território for conquistado, realiza a mudança de posse e movimentação de tropas.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> attackTerritory(
            @Parameter(description = "ID da partida.") @PathVariable Long gameId, 
            @RequestBody(
                description = "Detalhes do ataque: ID dos territórios e número de dados (1 a 3).",
                required = true,
                content = @Content(schema = @Schema(implementation = AttackRequestDto.class))
            )
            @org.springframework.web.bind.annotation.RequestBody AttackRequestDto attackRequest,
            Principal principal) {
        
        String username = principal.getName();
        
        try {
            Game updatedGame = gameService.attackTerritory(gameId, username, attackRequest);
            
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", updatedGame);
            return ResponseEntity.ok(updatedGame);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}