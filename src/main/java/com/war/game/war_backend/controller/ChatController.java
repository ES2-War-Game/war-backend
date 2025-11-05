package com.war.game.war_backend.controller;

import com.war.game.war_backend.controller.dto.ChatMessageDto;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.services.GameService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Tag(
        name = "Chat",
        description =
                "Endpoints de Mensageria em Tempo Real (WebSockets/STOMP) para comunicação in-game.")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;

    @MessageMapping("/game/{gameId}/chat")
    public void sendChatMessage(
            @Parameter(description = "ID da partida.") @DestinationVariable Long gameId,
            @Parameter(description = "Conteúdo da mensagem de chat.") ChatMessageDto message,
            Principal principal) {

        String senderUsername = principal.getName();

        // Verifica se a partida existe
        Game game = gameService.findGameById(gameId);

        // Verifica se o remetente é um jogador na partida.
        boolean isPlayerInGame =
                game.getPlayerGames().stream()
                        .anyMatch(
                                pg ->
                                        pg.getPlayer().getUsername().equals(senderUsername)
                                                && pg.getStillInGame());

        if (!isPlayerInGame) {
            return;
        }

        message.setSenderUsername(senderUsername);

        game.getPlayerGames().stream()
                .filter(pg -> pg.getPlayer().getUsername().equals(senderUsername))
                .findFirst();
        // .ifPresent(pg -> message.setColor(pg.getColor())); // opcional (tem que mudar o banco de
        // dados para cada playergame ter uma cor)

        String destination = "/topic/game/" + gameId + "/chat";
        messagingTemplate.convertAndSend(destination, message);
    }
}
