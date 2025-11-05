package com.war.game.war_backend.controller.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStateResponseDto {
    private Long id;
    private String status;
    private LocalDateTime createdAt;
    private String name;
    private PlayerGameDto turnPlayer;
    private PlayerGameDto winner;
    private Integer cardSetExchangeCount;
    private List<PlayerGameDto> playerGames;
    private List<GameTerritoryDto> gameTerritories;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerGameDto {
        private Long id;
        private Integer turnOrder;
        private String color;
        private Boolean isOwner;
        private Integer unallocatedArmies;
        private Boolean conqueredTerritoryThisTurn;
        private Boolean stillInGame;
        private ObjectiveDto objective;
        private PlayerDto player;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerDto {
        private Long id;
        private String username;
        private String imageUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectiveDto {
        private Long id;
        private String description;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameTerritoryDto {
        private Long id;
        private TerritoryDto territory;
        private Long ownerId; // Apenas o ID do PlayerGame, não o objeto completo
        private Integer staticArmies;
        private Integer movedInArmies;
        private Integer unallocatedArmies;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TerritoryDto {
        private Long id;
        private String name;
        private String continent;
    }
}
