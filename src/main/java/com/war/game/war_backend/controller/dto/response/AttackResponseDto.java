package com.war.game.war_backend.controller.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttackResponseDto {
  private List<Integer> attackerDice;
  private List<Integer> defenderDice;
  private GameStateResponseDto gameState;
}
