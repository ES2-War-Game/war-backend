package com.war.game.war_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.war.game.war_backend.model.Card;
import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.repository.CardRepository;
import com.war.game.war_backend.repository.TerritoryRepository;

import java.util.List;

@Configuration
public class CardInitializerConfig {
  @Bean
  public CommandLineRunner cardInitializer(CardRepository cardRepository,
      TerritoryRepository territoryRepository) {
    return args -> {
      // Verificar se os dados já foram inicializados
      if (cardRepository.count() > 0) {
        return;
      }

      // Buscar todos os territórios
      List<Territory> territories = territoryRepository.findAll();

      // Arrays dos tipos de cartas para distribuição equilibrada
      String[] cardTypes = {"cannon", "infantry", "cavalry"};
      int currentTypeIndex = 0;

      // Criar cartas para cada território
      for (Territory territory : territories) {
        Card card = new Card();
        card.setType(cardTypes[currentTypeIndex]);
        card.setTerritory(territory);
        cardRepository.save(card);

        // Alternar entre os tipos para distribuir igualmente
        currentTypeIndex = (currentTypeIndex + 1) % cardTypes.length;
      }

      // Criar 2 cartas curinga (wild cards)
      Card wildCard1 = new Card();
      wildCard1.setType("wild");
      wildCard1.setTerritory(null); // Curingas não têm território específico
      cardRepository.save(wildCard1);

      Card wildCard2 = new Card();
      wildCard2.setType("wild");
      wildCard2.setTerritory(null); // Curingas não têm território específico
      cardRepository.save(wildCard2);
    };
  }
}