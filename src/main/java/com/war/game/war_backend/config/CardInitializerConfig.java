package com.war.game.war_backend.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.war.game.war_backend.model.Card;
import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.model.enums.CardType;
import com.war.game.war_backend.repository.CardRepository;
import com.war.game.war_backend.repository.TerritoryRepository;

@Configuration
public class CardInitializerConfig {
  @Bean
  @Order(2) // Executa após o TerritoryInitializer (que deve ter @Order(1))
  public CommandLineRunner cardInitializer(
      CardRepository cardRepository, TerritoryRepository territoryRepository) {
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
      System.out.println(
          "=== CardInitializer: Territórios encontrados: " + territories.size() + " ===");

      if (territories.isEmpty()) {
        System.out.println("=== CardInitializer: ERRO - Nenhum território encontrado! ===");
        return;
      }

      // Mapeamento oficial War Grow/Estrela
      Map<String, CardType> territoryTypeMap = new java.util.HashMap<>();

      // INFANTRY (Triângulo)
      territoryTypeMap.put("ALASKA", CardType.INFANTRY);
      territoryTypeMap.put("VANCOUVER", CardType.INFANTRY);
      territoryTypeMap.put("BOLÍVIA", CardType.INFANTRY);
      territoryTypeMap.put("MOSCOU", CardType.INFANTRY);
      territoryTypeMap.put("SIBÉRIA", CardType.INFANTRY);
      territoryTypeMap.put("NOVA YORK", CardType.INFANTRY);
      territoryTypeMap.put("VENEZUELA", CardType.INFANTRY);
      territoryTypeMap.put("ISLÂNDIA", CardType.INFANTRY);
      territoryTypeMap.put("EGITO", CardType.INFANTRY);
      territoryTypeMap.put("ÁFRICA DO SUL", CardType.INFANTRY);
      territoryTypeMap.put("TCHITA", CardType.INFANTRY);
      territoryTypeMap.put("ARAL", CardType.INFANTRY);
      territoryTypeMap.put("AUSTRÁLIA", CardType.INFANTRY);

      // CAVALRY (Círculo)
      territoryTypeMap.put("LABRADOR", CardType.CAVALRY);
      territoryTypeMap.put("CALIFÓRNIA", CardType.CAVALRY);
      territoryTypeMap.put("MÉXICO", CardType.CAVALRY);
      territoryTypeMap.put("ARGENTINA", CardType.CAVALRY);
      territoryTypeMap.put("ITÁLIA", CardType.CAVALRY);
      territoryTypeMap.put("ESPANHA", CardType.CAVALRY);
      territoryTypeMap.put("CONGO", CardType.CAVALRY);
      territoryTypeMap.put("OMSK", CardType.CAVALRY);
      territoryTypeMap.put("SUDÃO", CardType.CAVALRY);
      territoryTypeMap.put("JAPÃO", CardType.CAVALRY);
      territoryTypeMap.put("ORIENTE MÉDIO", CardType.CAVALRY);
      territoryTypeMap.put("SUMATRA", CardType.CAVALRY);
      territoryTypeMap.put("BORNEO", CardType.CAVALRY);

      // CANNON (Quadrado)
      territoryTypeMap.put("MACKENZIE", CardType.CANNON);
      territoryTypeMap.put("GROENLÂNDIA", CardType.CANNON);
      territoryTypeMap.put("BRASIL", CardType.CANNON);
      territoryTypeMap.put("SUÉCIA", CardType.CANNON);
      territoryTypeMap.put("POLÔNIA", CardType.CANNON);
      territoryTypeMap.put("MADAGASCAR", CardType.CANNON);
      territoryTypeMap.put("OTTAWA", CardType.CANNON);
      territoryTypeMap.put("INGLATERRA", CardType.CANNON);
      territoryTypeMap.put("NIGÉRIA", CardType.CANNON);
      territoryTypeMap.put("DUDINKA", CardType.CANNON);
      territoryTypeMap.put("MONGÓLIA", CardType.CANNON);
      territoryTypeMap.put("ÍNDIA", CardType.CANNON);
      territoryTypeMap.put("NOVA GUINÉ", CardType.CANNON);

      System.out.println("=== CardInitializer: Criando cartas para territórios (War oficial) ===");
      // Mapeamento de nomes de imagem disponíveis
      java.util.Set<String> availableImages =
          new java.util.HashSet<>(
              java.util.Arrays.asList(
                  "Alaska.png",
                  "Mackenzie.png",
                  "Vancouver.png",
                  "Ottawa.png",
                  "Labrador.png",
                  "California.png",
                  "Nova York.png",
                  "Mexico.png",
                  "Groelandia.png",
                  "Venezuela.png",
                  "Brasil.png",
                  "Bolivia.png",
                  "Argentina.png",
                  "Islandia.png",
                  "Inglaterra.png",
                  "Suecia.png",
                  "Polonia.png",
                  "Italia.png",
                  "Espanha.png",
                  "Moscou.png",
                  "Egito.png",
                  "Nigeria.png",
                  "Sudao.png",
                  "Congo.png",
                  "AfricadoSul.png",
                  "Madagascar.png",
                  "Omsk.png",
                  "Dudinka.png",
                  "Siberia.png",
                  "Tchita.png",
                  "Mongolia.png",
                  "Japao.png",
                  "Aral.png",
                  "India.png",
                  "Oriente Medio.png",
                  "Australia.png",
                  "Sumatra.png",
                  "Borneo.png",
                  "Nova Guine.png"));

      // Função para mapear nome do território para nome do arquivo
      java.util.Map<String, String> territoryToImage = new java.util.HashMap<>();
      territoryToImage.put("ALASKA", "Alaska.png");
      territoryToImage.put("MACKENZIE", "Mackenzie.png");
      territoryToImage.put("VANCOUVER", "Vancouver.png");
      territoryToImage.put("OTTAWA", "Ottawa.png");
      territoryToImage.put("LABRADOR", "Labrador.png");
      territoryToImage.put("CALIFÓRNIA", "California.png");
      territoryToImage.put("NOVA YORK", "Nova York.png");
      territoryToImage.put("MÉXICO", "Mexico.png");
      territoryToImage.put("GROENLÂNDIA", "Groelandia.png");
      territoryToImage.put("VENEZUELA", "Venezuela.png");
      territoryToImage.put("BRASIL", "Brasil.png");
      territoryToImage.put("BOLÍVIA", "Bolivia.png");
      territoryToImage.put("ARGENTINA", "Argentina.png");
      territoryToImage.put("ISLÂNDIA", "Islandia.png");
      territoryToImage.put("INGLATERRA", "Inglaterra.png");
      territoryToImage.put("SUÉCIA", "Suecia.png");
      territoryToImage.put("POLÔNIA", "Polonia.png");
      territoryToImage.put("ITÁLIA", "Italia.png");
      territoryToImage.put("ESPANHA", "Espanha.png");
      territoryToImage.put("MOSCOU", "Moscou.png");
      territoryToImage.put("EGITO", "Egito.png");
      territoryToImage.put("NIGÉRIA", "Nigeria.png");
      territoryToImage.put("SUDÃO", "Sudao.png");
      territoryToImage.put("CONGO", "Congo.png");
      territoryToImage.put("ÁFRICA DO SUL", "AfricadoSul.png");
      territoryToImage.put("MADAGASCAR", "Madagascar.png");
      territoryToImage.put("OMSK", "Omsk.png");
      territoryToImage.put("DUDINKA", "Dudinka.png");
      territoryToImage.put("SIBÉRIA", "Siberia.png");
      territoryToImage.put("TCHITA", "Tchita.png");
      territoryToImage.put("MONGÓLIA", "Mongolia.png");
      territoryToImage.put("JAPÃO", "Japao.png");
      territoryToImage.put("ARAL", "Aral.png");
      territoryToImage.put("ÍNDIA", "India.png");
      territoryToImage.put("ORIENTE MÉDIO", "Oriente Medio.png");
      territoryToImage.put("AUSTRÁLIA", "Australia.png");
      territoryToImage.put("SUMATRA", "Sumatra.png");
      territoryToImage.put("BORNEO", "Borneo.png");
      territoryToImage.put("NOVA GUINÉ", "Nova Guine.png");

      for (Territory territory : territories) {
        String imageName = territoryToImage.get(territory.getName());
        if (imageName != null && availableImages.contains(imageName)) {
          Card card = new Card();
          CardType type = territoryTypeMap.getOrDefault(territory.getName(), CardType.INFANTRY);
          card.setType(type);
          card.setTerritory(territory);
          card.setImageName(imageName);
          cardRepository.save(card);
          System.out.println(
              "Carta criada: "
                  + type
                  + " para "
                  + territory.getName()
                  + " (imagem: "
                  + imageName
                  + ")");
        } else {
          System.out.println(
              "Carta NÃO criada para " + territory.getName() + " (imagem não encontrada)");
        }
      }

      System.out.println("=== CardInitializer: Criando cartas curinga ===");

      // Criar 2 cartas curinga (wild cards)
      Card wildCard1 = new Card();
      wildCard1.setType(CardType.WILD);
      wildCard1.setTerritory(null); // Curingas não têm território específico
      wildCard1.setImageName("Joker.png");
      cardRepository.save(wildCard1);
      System.out.println("Carta curinga 1 criada");

      Card wildCard2 = new Card();
      wildCard2.setType(CardType.WILD);
      wildCard2.setTerritory(null); // Curingas não têm território específico
      wildCard2.setImageName("Joker.png");
      cardRepository.save(wildCard2);
      System.out.println("Carta curinga 2 criada");

      System.out.println(
          "=== CardInitializer: Finalizado! Total de cartas criadas: "
              + (territories.size() + 2)
              + " ===");
    };
  }
}
