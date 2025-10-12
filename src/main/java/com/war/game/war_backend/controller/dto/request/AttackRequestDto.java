package com.war.game.war_backend.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttackRequestDto {
    
    @NotNull(message = "O ID do território atacante é obrigatório.")
    private Long sourceTerritoryId;
    
    @NotNull(message = "O ID do território defensor é obrigatório.")
    private Long targetTerritoryId;
    
    @NotNull(message = "O número de dados de ataque é obrigatório.")
    @Min(value = 1, message = "O ataque deve usar no mínimo 1 dado.")
    @Max(value = 3, message = "O ataque pode usar no máximo 3 dados.")
    private Integer attackDiceCount;

    @NotNull(message = "O número de exércitos a mover após a conquista é obrigatório.")
    @Min(value = 1, message = "Deve-se mover no mínimo 1 exército para o território conquistado.")
    private Integer troopsToMoveAfterConquest;
}