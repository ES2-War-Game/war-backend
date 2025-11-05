package com.war.game.war_backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String senderUsername;
    private String content;
    private long timestamp = System.currentTimeMillis();
    // private String color; // A cor do jogador para estilização no chat (ver depois)
}
