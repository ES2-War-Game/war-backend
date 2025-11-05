package com.war.game.war_backend.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LobbyListResponseDto {

    private Long id;
    private String name;
    private String status;
    private int playerCount;
}
