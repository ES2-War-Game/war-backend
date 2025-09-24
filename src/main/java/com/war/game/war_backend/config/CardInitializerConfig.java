package com.war.game.war_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.war.game.war_backend.model.Card;
import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.repository.CardRepository;
import com.war.game.war_backend.repository.TerritoryRepository;

import java.util.List;

@Configuration
public class CardInitializerConfig {
  @Bean
  @Order(2) // Executa após o TerritoryInitializer (que deve ter @Order(1))
  public CommandLineRunner cardInitializer(CardRepository cardRepository,
      TerritoryRepository territoryRepository) {
    return args -> {
      System.out.println("=== CardInitializer: Iniciando verificação ===");
      
      // Verificar se os dados já foram inicializados
      long cardCount = cardRepository.count();
      System.out.println("=== CardInitializer: Cartas existentes: " + cardCount + " ===");
      
      if (cardCount > 0) {
        System.out.println("=== CardInitializer: Cartas já existem, pulando inicialização ===");
        return;
      }

      // Buscar todos os territórios
      List<Territory> territories = territoryRepository.findAll();
      System.out.println("=== CardInitializer: Territórios encontrados: " + territories.size() + " ===");
      
      if (territories.isEmpty()) {
        System.out.println("=== CardInitializer: ERRO - Nenhum território encontrado! ===");
        return;
      }

      // Arrays dos tipos de cartas para distribuição equilibrada
      String[] cardTypes = {"cannon", "infantry", "cavalry"};
      int currentTypeIndex = 0;

      System.out.println("=== CardInitializer: Criando cartas para territórios ===");
      
      // Criar cartas para cada território
      for (Territory territory : territories) {
        Card card = new Card();
        card.setType(cardTypes[currentTypeIndex]);
        card.setTerritory(territory);
        cardRepository.save(card);
        
        System.out.println("Carta criada: " + cardTypes[currentTypeIndex] + " para " + territory.getName());

        // Alternar entre os tipos para distribuir igualmente
        currentTypeIndex = (currentTypeIndex + 1) % cardTypes.length;
      }

      System.out.println("=== CardInitializer: Criando cartas curinga ===");

      // Criar 2 cartas curinga (wild cards)
      Card wildCard1 = new Card();
      wildCard1.setType("wild");
      wildCard1.setTerritory(null); // Curingas não têm território específico
      cardRepository.save(wildCard1);
      System.out.println("Carta curinga 1 criada");

      Card wildCard2 = new Card();
      wildCard2.setType("wild");
      wildCard2.setTerritory(null); // Curingas não têm território específico
      cardRepository.save(wildCard2);
      System.out.println("Carta curinga 2 criada");
      
      System.out.println("=== CardInitializer: Finalizado! Total de cartas criadas: " + (territories.size() + 2) + " ===");
    };
  }
}