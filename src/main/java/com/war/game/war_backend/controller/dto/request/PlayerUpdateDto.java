package com.war.game.war_backend.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerUpdateDto {
  private String email;
  private String imageUrl;
  private String username;
}
