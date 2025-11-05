package com.war.game.war_backend.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementRequestDto {
  @NotNull private Long gameId;

  @NotNull private Long originTerritoryId;

  @NotNull private Long destinationTerritoryId;

  @NotNull
  @Min(1)
  private Integer troops;
}
