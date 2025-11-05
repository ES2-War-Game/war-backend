package com.war.game.war_backend.controller.dto.response;

import com.war.game.war_backend.model.Game;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameLobbyDetailsDto {

    private Long gameId;
    private String lobbyName;
    private String status;
    private int playerCount;

    private List<PlayerLobbyDtoResponse> players;

    public GameLobbyDetailsDto(Game game, List<PlayerLobbyDtoResponse> playerDtos) {
        this.gameId = game.getId();
        this.lobbyName = game.getName();
        this.status = game.getStatus();
        this.playerCount = playerDtos.size();
        this.players = playerDtos;
    }
}
