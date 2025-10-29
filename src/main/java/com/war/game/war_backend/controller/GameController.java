package com.war.game.war_backend.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.war.game.war_backend.controller.dto.request.AttackRequestDto;
import com.war.game.war_backend.controller.dto.request.LobbyCreationRequestDto;
import com.war.game.war_backend.controller.dto.response.GameLobbyDetailsDto;
import com.war.game.war_backend.controller.dto.response.GameStateResponseDto;
import com.war.game.war_backend.controller.dto.response.LobbyCreationResponseDto;
import com.war.game.war_backend.controller.dto.response.LobbyListResponseDto;
import com.war.game.war_backend.controller.dto.response.PlayerLobbyDtoResponse;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.enums.GameStatus;
import com.war.game.war_backend.services.GameService;
import com.war.game.war_backend.services.PlayerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
        String username = principal.getName();
        Player creator = playerService.getPlayerByUsername(username);

        Game newGame = gameService.createNewLobby(request.getLobbyName(), creator);

        List<PlayerLobbyDtoResponse> playerDtos = newGame.getPlayerGames().stream()
            .map(playerGame -> new PlayerLobbyDtoResponse(
                playerGame.getId(),
                playerGame.getPlayer().getUsername(),
                playerGame.getColor(),
                playerGame.getIsOwner(),
                playerGame.getPlayer().getImageUrl()
            ))
            .collect(Collectors.toList());

        LobbyCreationResponseDto response = new LobbyCreationResponseDto(
            newGame.getId(), 
            newGame.getName(),
            playerDtos
        );

        // Notifica todos os clientes sobre a atualização da lista de lobbies
        List<Game> allLobbies = gameService.findAllLobbies();
        List<LobbyListResponseDto> lobbyListDtos = allLobbies.stream()
            .map(lobby -> new LobbyListResponseDto(
                lobby.getId(),
                lobby.getName(),
                lobby.getStatus(),
                lobby.getPlayerGames().size()
            ))
            .collect(Collectors.toList());
        
        messagingTemplate.convertAndSend("/topic/lobbies/list", lobbyListDtos);

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
                lobby.getStatus(),
                lobby.getPlayerGames().size() 
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(lobbyDtos);
    }

    @GetMapping("/current-game")
    @Operation(summary = "Retorna o jogo/lobby ativo do jogador.", 
               description = "Retorna o jogo em que o jogador está participando (seja lobby ou partida em andamento). Retorna 404 se não estiver em nenhum jogo.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentGame(Principal principal) {
        String username = principal.getName();
        
        try {
            Player player = playerService.getPlayerByUsername(username);
            Game currentGame = gameService.findCurrentGameForPlayer(player);
            
            if (currentGame == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Você não está em nenhum jogo no momento.");
            }
            
            // Se for lobby, retorna informações básicas
            if (GameStatus.LOBBY.name().equals(currentGame.getStatus())) {
                List<PlayerLobbyDtoResponse> playerDtos = currentGame.getPlayerGames().stream()
                    .map(pg -> new PlayerLobbyDtoResponse(
                        pg.getId(),
                        pg.getPlayer().getUsername(),
                        pg.getColor(),
                        pg.getIsOwner(),
                        pg.getPlayer().getImageUrl()
                    ))
                    .collect(Collectors.toList());
                
                GameLobbyDetailsDto lobbyDetails = new GameLobbyDetailsDto(currentGame, playerDtos);
                return ResponseEntity.ok(lobbyDetails);
            }
            
            // Se for jogo ativo, retorna estado completo
            GameStateResponseDto gameState = convertToGameStateDto(currentGame);
            return ResponseEntity.ok(gameState);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/join/{lobbyId}")
    @Operation(summary = "Entra em um lobby existente.", description = "Adiciona o jogador autenticado ao lobby especificado. Envia notificação WebSocket.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GameLobbyDetailsDto> joinLobby(
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
                playerGame.getPlayer().getImageUrl()
            ))
            .collect(Collectors.toList());

        GameLobbyDetailsDto responseDto = new GameLobbyDetailsDto(updatedLobby, playerDtos);

        // Envia notificação WebSocket com o sufixo /state
        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/state", playerDtos);
        
        // Notifica sobre a atualização da lista global de lobbies (contagem de jogadores)
        List<Game> allLobbies = gameService.findAllLobbies();
        List<LobbyListResponseDto> lobbyListDtos = allLobbies.stream()
            .map(lobby -> new LobbyListResponseDto(
                lobby.getId(),
                lobby.getName(),
                lobby.getStatus(),
                lobby.getPlayerGames().size()
            ))
            .collect(Collectors.toList());
        messagingTemplate.convertAndSend("/topic/lobbies/list", lobbyListDtos);

        return ResponseEntity.ok(responseDto);
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
            // Envia notificação WebSocket com o sufixo /state (lobby excluído)
            messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/state", List.of());
            
            // Notifica sobre a atualização da lista global de lobbies
            List<Game> allLobbies = gameService.findAllLobbies();
            List<LobbyListResponseDto> lobbyListDtos = allLobbies.stream()
                .map(lobby -> new LobbyListResponseDto(
                    lobby.getId(),
                    lobby.getName(),
                    lobby.getStatus(),
                    lobby.getPlayerGames().size()
                ))
                .collect(Collectors.toList());
            messagingTemplate.convertAndSend("/topic/lobbies/list", lobbyListDtos);
            
            return ResponseEntity.ok(List.of()); 
        }

        List<PlayerLobbyDtoResponse> playerDtos = updatedLobby.getPlayerGames().stream()
            .map(playerGame -> new PlayerLobbyDtoResponse(
                playerGame.getId(), 
                playerGame.getPlayer().getUsername(), 
                playerGame.getColor(),
                playerGame.getIsOwner(),
                playerGame.getPlayer().getImageUrl()
            ))
            .collect(Collectors.toList());

        // Envia notificação WebSocket com o sufixo /state
        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/state", playerDtos);
        
        // Notifica sobre a atualização da lista global de lobbies
        List<Game> allLobbies = gameService.findAllLobbies();
        List<LobbyListResponseDto> lobbyListDtos = allLobbies.stream()
            .map(lobby -> new LobbyListResponseDto(
                lobby.getId(),
                lobby.getName(),
                lobby.getStatus(),
                lobby.getPlayerGames().size()
            ))
            .collect(Collectors.toList());
        messagingTemplate.convertAndSend("/topic/lobbies/list", lobbyListDtos);

        return ResponseEntity.ok(playerDtos);
    }

    @PostMapping("/leave-game/{gameId}")
    @Operation(summary = "Sai de um jogo já iniciado.", description = "Remove o jogador de um jogo em andamento. O turno dele será pulado e seus territórios redistribuídos.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GameStateResponseDto> leaveGame(
            @Parameter(description = "ID do jogo que o jogador deseja sair.") 
            @PathVariable Long gameId, 
            Principal principal) {
        
        String username = principal.getName();
        Player player = playerService.getPlayerByUsername(username);

        Game updatedGame = gameService.removePlayerFromGame(gameId, player);

        // Converte para DTO
        GameStateResponseDto gameStateDto = convertToGameStateDto(updatedGame);

        // Notifica todos os jogadores via WebSocket
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", gameStateDto);

        return ResponseEntity.ok(gameStateDto);
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
            GameStateResponseDto gameState = convertToGameStateDto(startedGame);
            
            messagingTemplate.convertAndSend("/topic/game/" + lobbyId + "/state", gameState);
            return ResponseEntity.ok(gameState);

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
            GameStateResponseDto gameState = convertToGameStateDto(updatedGame);
            
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", gameState);
            return ResponseEntity.ok(gameState);

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
            GameStateResponseDto gameState = convertToGameStateDto(updatedGame);

            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", gameState);

            return ResponseEntity.ok(gameState);

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
            GameStateResponseDto gameState = convertToGameStateDto(updatedGame);

            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", gameState);
            return ResponseEntity.ok(gameState);
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
            GameStateResponseDto gameState = convertToGameStateDto(updatedGame);
            
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", gameState);
            return ResponseEntity.ok(gameState);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Método auxiliar para converter Game em GameStateResponseDto (evita referências circulares)
    private GameStateResponseDto convertToGameStateDto(Game game) {
        GameStateResponseDto dto = new GameStateResponseDto();
        dto.setId(game.getId());
        dto.setStatus(game.getStatus());
        dto.setCreatedAt(game.getCreatedAt());
        dto.setName(game.getName());
        dto.setCardSetExchangeCount(game.getCardSetExchangeCount());
        
        // Converter turnPlayer
        if (game.getTurnPlayer() != null) {
            dto.setTurnPlayer(convertToPlayerGameDto(game.getTurnPlayer()));
        }
        
        // Converter winner
        if (game.getWinner() != null) {
            dto.setWinner(convertToPlayerGameDto(game.getWinner()));
        }
        
        // Converter playerGames
        if (game.getPlayerGames() != null) {
            dto.setPlayerGames(game.getPlayerGames().stream()
                .map(this::convertToPlayerGameDto)
                .collect(Collectors.toList()));
        }
        
        // Converter gameTerritories
        if (game.getGameTerritories() != null) {
            dto.setGameTerritories(game.getGameTerritories().stream()
                .map(this::convertToGameTerritoryDto)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    private GameStateResponseDto.PlayerGameDto convertToPlayerGameDto(com.war.game.war_backend.model.PlayerGame pg) {
        GameStateResponseDto.PlayerGameDto dto = new GameStateResponseDto.PlayerGameDto();
        dto.setId(pg.getId());
        dto.setTurnOrder(pg.getTurnOrder());
        dto.setColor(pg.getColor());
        dto.setIsOwner(pg.getIsOwner());
        dto.setUnallocatedArmies(pg.getUnallocatedArmies());
        dto.setConqueredTerritoryThisTurn(pg.getConqueredTerritoryThisTurn());
        dto.setStillInGame(pg.getStillInGame());
        
        if (pg.getObjective() != null) {
            GameStateResponseDto.ObjectiveDto objDto = new GameStateResponseDto.ObjectiveDto();
            objDto.setId(pg.getObjective().getId());
            objDto.setDescription(pg.getObjective().getDescription());
            objDto.setType(pg.getObjective().getType());
            dto.setObjective(objDto);
        }
        
        if (pg.getPlayer() != null) {
            GameStateResponseDto.PlayerDto playerDto = new GameStateResponseDto.PlayerDto();
            playerDto.setId(pg.getPlayer().getId());
            playerDto.setUsername(pg.getPlayer().getUsername());
            playerDto.setImageUrl(pg.getPlayer().getImageUrl());
            dto.setPlayer(playerDto);
        }
        
        return dto;
    }
    
    private GameStateResponseDto.GameTerritoryDto convertToGameTerritoryDto(com.war.game.war_backend.model.GameTerritory gt) {
        GameStateResponseDto.GameTerritoryDto dto = new GameStateResponseDto.GameTerritoryDto();
        dto.setId(gt.getId());
        dto.setStaticArmies(gt.getStaticArmies());
        dto.setMovedInArmies(gt.getMovedInArmies());
        dto.setUnallocatedArmies(gt.getUnallocatedArmies());
        
        // Apenas o ID do owner, não o objeto completo
        if (gt.getOwner() != null) {
            dto.setOwnerId(gt.getOwner().getId());
        }
        
        if (gt.getTerritory() != null) {
            GameStateResponseDto.TerritoryDto terrDto = new GameStateResponseDto.TerritoryDto();
            terrDto.setId(gt.getTerritory().getId());
            terrDto.setName(gt.getTerritory().getName());
            terrDto.setContinent(gt.getTerritory().getContinent());
            dto.setTerritory(terrDto);
        }
        
        return dto;
    }
}
