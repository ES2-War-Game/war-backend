package com.war.game.war_backend.controller.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LobbyCreationRequestDto {

    @NotBlank(message = "O nome do lobby é obrigatório")
    private String lobbyName;
}
