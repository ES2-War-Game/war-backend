package com.war.game.war_backend.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LobbyCreationRequestDto {

    @NotBlank(message = "O nome do lobby é obrigatório")
    private String lobbyName;
}
