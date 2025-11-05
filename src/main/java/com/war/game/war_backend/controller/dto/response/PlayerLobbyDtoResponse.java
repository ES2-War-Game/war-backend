package com.war.game.war_backend.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLobbyDtoResponse {
  private Long id;
  private String username;
  private String color;
  private boolean isOwner;
  private String imageUrl;
}
