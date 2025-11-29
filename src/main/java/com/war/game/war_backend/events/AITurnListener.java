package com.war.game.war_backend.events;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.war.game.war_backend.controller.dto.response.GameStateResponseDto;
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.enums.GameStatus;
import com.war.game.war_backend.services.GameService;

@Component
public class AITurnListener {

  @Autowired private GameService gameService;
  @Autowired private SimpMessagingTemplate messagingTemplate;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleAIActionRequest(AIActionRequestedEvent event) {
    System.out.println("========== LISTENER: Turno da IA iniciado.==========");

    Game finalGame = gameService.executeAIAction(event.getGameId(), event.getUsername());
    
    GameStateResponseDto gameState = convertToGameStateDto(finalGame);

    if (!GameStatus.FINISHED.name().equals(finalGame.getStatus())) {
      messagingTemplate.convertAndSend(
        "/topic/game/" + finalGame.getId() + "/state", 
        gameState
      );
    } else {
      System.out.println("Jogo encerrado");
    }
    
    System.out.println("========== LISTENER: Ação da IA concluída. ==========");
  }

  private GameStateResponseDto convertToGameStateDto(Game game) {
    GameStateResponseDto dto = new GameStateResponseDto();
    dto.setId(game.getId());
    dto.setStatus(game.getStatus());
    dto.setCreatedAt(game.getCreatedAt());
    dto.setName(game.getName());
    dto.setCardSetExchangeCount(game.getCardSetExchangeCount());

    // Converter turnPlayer
    if (game.getTurnPlayer() != null) {
      dto.setTurnPlayer(convertToPlayerGameDto(game.getTurnPlayer()));
    }

    // Converter winner
    if (game.getWinner() != null) {
      dto.setWinner(convertToPlayerGameDto(game.getWinner()));
    }

    // Converter playerGames
    if (game.getPlayerGames() != null) {
      dto.setPlayerGames(
          game.getPlayerGames().stream()
              .map(this::convertToPlayerGameDto)
              .collect(Collectors.toList()));
    }

    // Converter gameTerritories
    if (game.getGameTerritories() != null) {
      dto.setGameTerritories(
          game.getGameTerritories().stream()
              .map(this::convertToGameTerritoryDto)
              .collect(Collectors.toList()));
    }

    return dto;
  }

  private GameStateResponseDto.PlayerGameDto convertToPlayerGameDto(
      com.war.game.war_backend.model.PlayerGame pg) {
    GameStateResponseDto.PlayerGameDto dto = new GameStateResponseDto.PlayerGameDto();
    dto.setId(pg.getId());
    dto.setTurnOrder(pg.getTurnOrder());
    dto.setColor(pg.getColor());
    dto.setIsOwner(pg.getIsOwner());
    dto.setUnallocatedArmies(pg.getUnallocatedArmies());
    dto.setConqueredTerritoryThisTurn(pg.getConqueredTerritoryThisTurn());
    dto.setStillInGame(pg.getStillInGame());

    if (pg.getObjective() != null) {
      GameStateResponseDto.ObjectiveDto objDto = new GameStateResponseDto.ObjectiveDto();
      objDto.setId(pg.getObjective().getId());
      objDto.setDescription(pg.getObjective().getDescription());
      objDto.setType(pg.getObjective().getType());
      dto.setObjective(objDto);
    }

    if (pg.getPlayer() != null) {
      GameStateResponseDto.PlayerDto playerDto = new GameStateResponseDto.PlayerDto();
      playerDto.setId(pg.getPlayer().getId());
      playerDto.setUsername(pg.getPlayer().getUsername());
      playerDto.setImageUrl(pg.getPlayer().getImageUrl());
      dto.setPlayer(playerDto);
    }

    List<com.war.game.war_backend.model.PlayerCard> playerCards = gameService.getPlayerCards(pg);
    List<GameStateResponseDto.PlayerCardDto> cardDtos =
        playerCards.stream()
            .map(
                pc -> {
                  GameStateResponseDto.PlayerCardDto pcDto =
                      new GameStateResponseDto.PlayerCardDto();
                  pcDto.setId(pc.getId());
                  if (pc.getCard() != null) {
                    GameStateResponseDto.CardDto cardDto = new GameStateResponseDto.CardDto();
                    cardDto.setId(pc.getCard().getId());
                    cardDto.setType(pc.getCard().getType().name());
                    cardDto.setImageName(pc.getCard().getImageName());
                    if (pc.getCard().getTerritory() != null) {
                      GameStateResponseDto.TerritoryDto terrDto =
                          new GameStateResponseDto.TerritoryDto();
                      terrDto.setId(pc.getCard().getTerritory().getId());
                      terrDto.setName(pc.getCard().getTerritory().getName());
                      terrDto.setContinent(pc.getCard().getTerritory().getContinent());
                      cardDto.setTerritory(terrDto);
                    }
                    pcDto.setCard(cardDto);
                  }
                  return pcDto;
                })
            .collect(java.util.stream.Collectors.toList());
    dto.setPlayerCards(cardDtos);

    return dto;
  }

  private GameStateResponseDto.GameTerritoryDto convertToGameTerritoryDto(
      com.war.game.war_backend.model.GameTerritory gt) {
    GameStateResponseDto.GameTerritoryDto dto = new GameStateResponseDto.GameTerritoryDto();
    dto.setId(gt.getTerritory() != null ? gt.getTerritory().getId() : null);
    dto.setStaticArmies(gt.getStaticArmies());
    dto.setMovedInArmies(gt.getMovedInArmies());
    dto.setUnallocatedArmies(gt.getUnallocatedArmies());

    if (gt.getOwner() != null) {
      dto.setOwnerId(gt.getOwner().getId());
    }

    if (gt.getTerritory() != null) {
      GameStateResponseDto.TerritoryDto terrDto = new GameStateResponseDto.TerritoryDto();
      terrDto.setId(gt.getTerritory().getId());
      terrDto.setName(gt.getTerritory().getName());
      terrDto.setContinent(gt.getTerritory().getContinent());
      dto.setTerritory(terrDto);
    }

    return dto;
  }
}
