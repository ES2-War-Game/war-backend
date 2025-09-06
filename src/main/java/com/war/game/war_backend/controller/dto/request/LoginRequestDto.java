package com.war.game.war_backend.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "Usuario não pode estar vazio")
    private String username;

    @NotBlank(message = "Senha não pode estar vazia")
    private String password;
}