package com.war.game.war_backend.controller.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerUpdateDto {
  private String email;
  private String imageUrl;
}
