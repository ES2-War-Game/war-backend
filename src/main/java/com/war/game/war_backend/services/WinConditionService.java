package com.war.game.war_backend.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Mantido para lógica transacional
import com.war.game.war_backend.events.GameOverEvent; // NOVO: Importe o Evento
import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.Objective;
import com.war.game.war_backend.model.PlayerGame;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WinConditionService {

    // Simulação do mapeamento Continente -> Total de Territórios
    private static final Map<String, Long> CONTINENT_TERRITORY_COUNT = Map.of(
        "América do Sul", 4L,
        "América do Norte", 9L,
        "Europa", 7L,
        "África", 6L,
        "Ásia", 12L,
        "Oceania", 4L
    );

    private final ApplicationEventPublisher eventPublisher; 
    
    public WinConditionService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public void checkWinConditions(Game game, PlayerGame actingPlayerGame) {
        
        // Verificar Condição Padrão: Sobrevivência
        if (checkEliminationWin(game)) {
            return; 
        }
        
        // Verificar o Objetivo Secreto
        checkObjectiveCompletion(game, actingPlayerGame); 
    }
    
    // Lógica de Verificação de Sobrevivência
    private boolean checkEliminationWin(Game game) {
        long activePlayers = game.getPlayerGames().stream() 
                               .filter(PlayerGame::getStillInGame)
                               .count();
        
        if (activePlayers == 1) {
            PlayerGame winner = game.getPlayerGames().stream()
                                .filter(PlayerGame::getStillInGame)
                                .findFirst().get();

            eventPublisher.publishEvent(new GameOverEvent(
                this, 
                game, 
                winner, 
                "ELIMINATION_COMPLETE", 
                "Último jogador restante."
            ));
            
            return true;
        }
        return false;
    }
    
    // Lógica de Verificação do Objetivo Secreto (Refatorada para disparar o Evento)
    public boolean checkObjectiveCompletion(Game game, PlayerGame playerGame) {   
        Objective objective = playerGame.getObjective();
        
        if (objective == null) return false;
        
        boolean completed = false;
        String objectiveDescription = objective.getDescription();

        switch (objective.getType()) {
            case "CONQUER_CONTINENT":
                completed = checkConquerContinent(playerGame, objective);
                break;
            case "CONQUER_TERRITORIES":
                completed = checkConquerTerritories(playerGame, objective);
                break;
            case "ELIMINATE_PLAYER":
                completed = checkEliminatePlayer(game, playerGame, objective) 
                            && !playerGame.getStillInGame();
                break;
        }
        
        if (completed) {
            eventPublisher.publishEvent(new GameOverEvent(
                this, 
                game, 
                playerGame,
                "OBJECTIVE_COMPLETED", 
                objectiveDescription
            ));
            
            return true;
        }
        return false;
    }
    
    // Verifica objetivos de conquista por quantidade de territórios
    private boolean checkConquerTerritories(PlayerGame playerGame, Objective objective) {
        String description = objective.getDescription();

        if (description.contains("24 territórios") || description.contains("26 Territórios")) {
            return playerGame.getOwnedTerritories().size() >= 24; 
        }
        if (description.contains("18 territórios com pelo menos 2 exércitos")) {
             long qualifiedTerritories = playerGame.getOwnedTerritories().stream()
                                            .filter(t -> t.getArmies() >= 2) 
                                            .count();
             return qualifiedTerritories >= 18;
        }
        return false;
    }
    
    // Verifica objetivo de eliminação de um jogador específico
    private boolean checkEliminatePlayer(Game game, PlayerGame actingPlayerGame, Objective objective) {
        String description = objective.getDescription();
        String targetColor = description.substring(description.lastIndexOf(" ") + 1).trim(); 
        
        return game.getPlayerGames().stream()
                   .filter(pg -> pg.getColor().equalsIgnoreCase(targetColor))
                   .findFirst()
                   .map(targetPlayerGame -> !targetPlayerGame.getStillInGame()) 
                   .orElse(false);
    }
    
    // Verifica se o jogador conquistou os continentes necessários (COMPLEXO)
    private boolean checkConquerContinent(PlayerGame playerGame, Objective objective) {
        String description = objective.getDescription();
        List<String> requiredContinents = extractRequiredContinents(description);

        Map<String, Long> ownedTerritoriesPerContinent = playerGame.getOwnedTerritories().stream()
            .collect(Collectors.groupingBy(
                gt -> gt.getTerritory().getContinent(),
                Collectors.counting()
            ));

        int successfullyConquered = 0;
        
        for (String continent : requiredContinents) {
            if (ownedTerritoriesPerContinent.containsKey(continent)) {
                Long ownedCount = ownedTerritoriesPerContinent.get(continent);
                Long totalCount = CONTINENT_TERRITORY_COUNT.getOrDefault(continent, 0L);
                
                if (ownedCount.equals(totalCount) && totalCount > 0) {
                    successfullyConquered++;
                }
            }
        }
        
        if (description.contains("e mais um continente")) {
            long totalContinentsControlled = ownedTerritoriesPerContinent.keySet().stream()
                .filter(continent -> ownedTerritoriesPerContinent.get(continent).equals(CONTINENT_TERRITORY_COUNT.getOrDefault(continent, 0L)) && CONTINENT_TERRITORY_COUNT.getOrDefault(continent, 0L) > 0)
                .count();
                
            return totalContinentsControlled >= 3;
        }
        
        return successfullyConquered == requiredContinents.size();
    }
    
    // Auxiliar para extrair continentes da descrição
    private List<String> extractRequiredContinents(String description) {
        List<String> allContinents = List.of("América do Sul", "América do Norte", "Europa", "África", "Ásia", "Oceania");
        
        List<String> required = allContinents.stream()
            .filter(description::contains)
            .collect(Collectors.toList());
            
        return required;
    }
}