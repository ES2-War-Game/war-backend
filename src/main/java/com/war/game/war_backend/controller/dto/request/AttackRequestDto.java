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

    // REMOVIDO: troopsToMoveAfterConquest
    // O backend agora calcula automaticamente quantas tropas mover após conquista:
    // Move todas as tropas que participaram do ataque (deixando apenas 1 no território de origem)
}
