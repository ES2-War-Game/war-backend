package com.war.game.war_backend.controller.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LobbyCreationResponseDto {

  private Long gameId;
  private String lobbyName;

  private List<PlayerLobbyDtoResponse> players;
}
