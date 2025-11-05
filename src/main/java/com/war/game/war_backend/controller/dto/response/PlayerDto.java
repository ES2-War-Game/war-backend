package com.war.game.war_backend.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {
    private Long id;
    private String username;
    private String email;
    private String imageUrl;
}
