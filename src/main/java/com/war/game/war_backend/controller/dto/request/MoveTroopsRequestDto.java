package com.war.game.war_backend.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Requisição para movimentar tropas entre territórios adjacentes do mesmo jogador")
public class MoveTroopsRequestDto {

  @NotNull(message = "O ID do território de origem é obrigatório.")
  @Schema(description = "ID do território de onde as tropas serão movidas", example = "6")
  private Long sourceTerritoryId;

  @NotNull(message = "O ID do território de destino é obrigatório.")
  @Schema(description = "ID do território para onde as tropas serão movidas", example = "3")
  private Long targetTerritoryId;

  @NotNull(message = "O número de tropas é obrigatório.")
  @Min(value = 1, message = "É necessário mover pelo menos 1 tropa.")
  @Schema(
      description =
          "Número de tropas a serem movidas (deve deixar pelo menos 1 no território de origem)",
      example = "5",
      minimum = "1")
  private Integer troopCount;
}
