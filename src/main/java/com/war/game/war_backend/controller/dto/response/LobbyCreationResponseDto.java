package com.war.game.war_backend.controller.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LobbyCreationResponseDto {

    private Long gameId;
    private String lobbyName;
}