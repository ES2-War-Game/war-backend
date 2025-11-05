package com.war.game.war_backend.model.enums;

public enum GameStatus {

  // FASES DE PRÉ-JOGO E GERENCIAMENTO

  // Jogo criado, aguardando jogadores no lobby.
  LOBBY,

  // Jogo pausado ou cancelado (talvez usado depois).
  CANCELED,

  // FASES DE INÍCIO DA PARTIDA

  // Fase de alocação inicial de exércitos em todos os territórios.
  SETUP_ALLOCATION,

  // FASES DE TURNO

  // ase principal do jogo, onde os turnos estão rodando.
  IN_PROGRESS,

  // Sub-fase de reforço no início do turno.
  REINFORCEMENT,

  // Sub-fase de ataque.
  ATTACK,

  // Sub-fase de movimentação e reagrupamento de tropas. O turno termina após esta fase.
  MOVEMENT,

  // FASE DE FIM DE JOGO

  // O jogo foi concluído, um vencedor foi determinado.
  FINISHED
}
