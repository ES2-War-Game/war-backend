package com.war.game.war_backend.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRegistrationDto {

    @NotBlank(message = "Usuario não pode estar vazio")
    @Size(min = 3, max = 50, message = "Usuario deve ter entre 3 e 50 caracteres")
    private String username;

    @NotBlank(message = "Email não pode estar vazio")
    @Email(message = "Email deve ser um endereço de email valido")
    private String email;

    @NotBlank(message = "Senha não pode estar vazia")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String password;
}