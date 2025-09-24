package com.war.game.war_backend.config;

import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.model.TerritoryBorder;
import com.war.game.war_backend.repository.TerritoryRepository;
import com.war.game.war_backend.repository.TerritoryBorderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class TerritoryInitializerConfig {  
  @Bean
  @Order(1) // Executa primeiro
  public CommandLineRunner territoryInitializer(TerritoryRepository territoryRepository,
      TerritoryBorderRepository borderRepository) {
    return args -> {
      System.out.println("=== TerritoryInitializer: Iniciando verificação ===");
      
      // Verificar se os dados já foram inicializados
      long territoryCount = territoryRepository.count();
      long borderCount = borderRepository.count();
      
      System.out.println("=== TerritoryInitializer: Territórios existentes: " + territoryCount + " ===");
      System.out.println("=== TerritoryInitializer: Bordas existentes: " + borderCount + " ===");
      
      if (territoryCount > 0) {
        System.out.println("=== TerritoryInitializer: Territórios já existem, pulando inicialização de territórios ===");
        
        // Verificar se as bordas foram criadas
        if (borderCount == 0) {
          System.out.println("=== TerritoryInitializer: CRIANDO APENAS AS BORDAS ===");
          // Buscar territórios existentes para criar as bordas
          createBorders(territoryRepository, borderRepository);
        } else {
          System.out.println("=== TerritoryInitializer: Bordas já existem, pulando tudo ===");
        }
        return;
      }

      System.out.println("=== TerritoryInitializer: Criando territórios ===");
      
      // América do Norte
      territoryRepository.save(new Territory(null, "ALASKA", "América do Norte", null, null));
      territoryRepository.save(new Territory(null, "MACKENZIE", "América do Norte", null, null));
      territoryRepository.save(new Territory(null, "VANCOUVER", "América do Norte", null, null));
      territoryRepository.save(new Territory(null, "OTTAWA", "América do Norte", null, null));
      territoryRepository.save(new Territory(null, "LABRADOR", "América do Norte", null, null));
      territoryRepository.save(new Territory(null, "CALIFÓRNIA", "América do Norte", null, null));
      territoryRepository.save(new Territory(null, "NOVA YORK", "América do Norte", null, null));
      territoryRepository.save(new Territory(null, "MÉXICO", "América do Norte", null, null));
      territoryRepository.save(new Territory(null, "GROENLÂNDIA", "América do Norte", null, null));

      // América do Sul
      territoryRepository.save(new Territory(null, "VENEZUELA", "América do Sul", null, null));
      territoryRepository.save(new Territory(null, "BRASIL", "América do Sul", null, null));
      territoryRepository.save(new Territory(null, "BOLÍVIA", "América do Sul", null, null));
      territoryRepository.save(new Territory(null, "ARGENTINA", "América do Sul", null, null));

      // Europa
      territoryRepository.save(new Territory(null, "ISLÂNDIA", "Europa", null, null));
      territoryRepository.save(new Territory(null, "INGLATERRA", "Europa", null, null));
      territoryRepository.save(new Territory(null, "SUÉCIA", "Europa", null, null));
      territoryRepository.save(new Territory(null, "POLÔNIA", "Europa", null, null));
      territoryRepository.save(new Territory(null, "ITÁLIA", "Europa", null, null));
      territoryRepository.save(new Territory(null, "ESPANHA", "Europa", null, null));
      territoryRepository.save(new Territory(null, "MOSCOU", "Europa", null, null));

      // África
      territoryRepository.save(new Territory(null, "EGITO", "África", null, null));
      territoryRepository.save(new Territory(null, "NIGÉRIA", "África", null, null));
      territoryRepository.save(new Territory(null, "SUDÃO", "África", null, null));
      territoryRepository.save(new Territory(null, "CONGO", "África", null, null));
      territoryRepository.save(new Territory(null, "ÁFRICA DO SUL", "África", null, null));
      territoryRepository.save(new Territory(null, "MADAGASCAR", "África", null, null));

      // Ásia
      territoryRepository.save(new Territory(null, "OMSK", "Ásia", null, null));
      territoryRepository.save(new Territory(null, "DUDINKA", "Ásia", null, null));
      territoryRepository.save(new Territory(null, "SIBÉRIA", "Ásia", null, null));
      territoryRepository.save(new Territory(null, "VLADIVOSTOK", "Ásia", null, null));
      territoryRepository.save(new Territory(null, "TCHITA", "Ásia", null, null));
      territoryRepository.save(new Territory(null, "MONGÓLIA", "Ásia", null, null));
      territoryRepository.save(new Territory(null, "JAPÃO", "Ásia", null, null));
      territoryRepository.save(new Territory(null, "ARAL", "Ásia", null, null));
      territoryRepository.save(new Territory(null, "CHINA", "Ásia", null, null));
      territoryRepository.save(new Territory(null, "ÍNDIA", "Ásia", null, null));
      territoryRepository.save(new Territory(null, "VIETNÃ", "Ásia", null, null));
      territoryRepository.save(new Territory(null, "ORIENTE MÉDIO", "Ásia", null, null));

      // Oceania
      territoryRepository.save(new Territory(null, "AUSTRÁLIA", "Oceania", null, null));
      territoryRepository.save(new Territory(null, "SUMATRA", "Oceania", null, null));
      territoryRepository.save(new Territory(null, "BORNEO", "Oceania", null, null));
      territoryRepository.save(new Territory(null, "NOVA GUINÉ", "Oceania", null, null));

      System.out.println("=== TerritoryInitializer: 40 territórios criados com sucesso ===");
      System.out.println("=== TerritoryInitializer: Criando bordas ===");

      // Criar bordas
      createBorders(territoryRepository, borderRepository);
      
      System.out.println("=== TerritoryInitializer: Finalizado com sucesso ===");
    };
  }

  private void createBorders(TerritoryRepository territoryRepository, TerritoryBorderRepository borderRepository) {
    // Buscar todos os territórios pelo nome
    Territory alaska = territoryRepository.findByName("ALASKA").orElse(null);
    Territory mackenzie = territoryRepository.findByName("MACKENZIE").orElse(null);
    Territory vancouver = territoryRepository.findByName("VANCOUVER").orElse(null);
    Territory ottawa = territoryRepository.findByName("OTTAWA").orElse(null);
    Territory labrador = territoryRepository.findByName("LABRADOR").orElse(null);
    Territory califórnia = territoryRepository.findByName("CALIFÓRNIA").orElse(null);
    Territory novaYork = territoryRepository.findByName("NOVA YORK").orElse(null);
    Territory méxico = territoryRepository.findByName("MÉXICO").orElse(null);
    Territory groenlandia = territoryRepository.findByName("GROENLÂNDIA").orElse(null);

    Territory venezuela = territoryRepository.findByName("VENEZUELA").orElse(null);
    Territory brasil = territoryRepository.findByName("BRASIL").orElse(null);
    Territory bolivia = territoryRepository.findByName("BOLÍVIA").orElse(null);
    Territory argentina = territoryRepository.findByName("ARGENTINA").orElse(null);

    Territory islândia = territoryRepository.findByName("ISLÂNDIA").orElse(null);
    Territory inglaterra = territoryRepository.findByName("INGLATERRA").orElse(null);
    Territory suécia = territoryRepository.findByName("SUÉCIA").orElse(null);
    Territory polônia = territoryRepository.findByName("POLÔNIA").orElse(null);
    Territory itália = territoryRepository.findByName("ITÁLIA").orElse(null);
    Territory espanha = territoryRepository.findByName("ESPANHA").orElse(null);
    Territory moscou = territoryRepository.findByName("MOSCOU").orElse(null);

    Territory egito = territoryRepository.findByName("EGITO").orElse(null);
    Territory nigéria = territoryRepository.findByName("NIGÉRIA").orElse(null);
    Territory sudão = territoryRepository.findByName("SUDÃO").orElse(null);
    Territory congo = territoryRepository.findByName("CONGO").orElse(null);
    Territory africaSul = territoryRepository.findByName("ÁFRICA DO SUL").orElse(null);
    Territory madagascar = territoryRepository.findByName("MADAGASCAR").orElse(null);

    Territory omsk = territoryRepository.findByName("OMSK").orElse(null);
    Territory dudinka = territoryRepository.findByName("DUDINKA").orElse(null);
    Territory siberia = territoryRepository.findByName("SIBÉRIA").orElse(null);
    Territory vladivostok = territoryRepository.findByName("VLADIVOSTOK").orElse(null);
    Territory tchita = territoryRepository.findByName("TCHITA").orElse(null);
    Territory mongólia = territoryRepository.findByName("MONGÓLIA").orElse(null);
    Territory japao = territoryRepository.findByName("JAPÃO").orElse(null);
    Territory aral = territoryRepository.findByName("ARAL").orElse(null);
    Territory china = territoryRepository.findByName("CHINA").orElse(null);
    Territory india = territoryRepository.findByName("ÍNDIA").orElse(null);
    Territory vietna = territoryRepository.findByName("VIETNÃ").orElse(null);
    Territory orienteMedio = territoryRepository.findByName("ORIENTE MÉDIO").orElse(null);

    Territory australia = territoryRepository.findByName("AUSTRÁLIA").orElse(null);
    Territory sumatra = territoryRepository.findByName("SUMATRA").orElse(null);
    Territory borneo = territoryRepository.findByName("BORNEO").orElse(null);
    Territory novaGuine = territoryRepository.findByName("NOVA GUINÉ").orElse(null);

    int bordersCreated = 0;

    // Fronteiras conforme o mapa do War

    // América do Norte - Fronteiras internas
    borderRepository.save(new TerritoryBorder(null, alaska, mackenzie)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, alaska, vancouver)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, mackenzie, vancouver)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, mackenzie, ottawa)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, mackenzie, groenlandia)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, vancouver, ottawa)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, vancouver, califórnia)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, ottawa, labrador)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, ottawa, novaYork)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, ottawa, califórnia)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, ottawa, groenlandia)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, labrador, groenlandia)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, labrador, novaYork)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, califórnia, novaYork)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, califórnia, méxico)); bordersCreated++;
    borderRepository.save(new TerritoryBorder(null, novaYork, méxico)); bordersCreated++;

      // América do Sul - Fronteiras internas
      borderRepository.save(new TerritoryBorder(null, venezuela, brasil));
      borderRepository.save(new TerritoryBorder(null, venezuela, bolivia));
      borderRepository.save(new TerritoryBorder(null, brasil, bolivia));
      borderRepository.save(new TerritoryBorder(null, brasil, argentina));
      borderRepository.save(new TerritoryBorder(null, bolivia, argentina));

      // Europa - Fronteiras internas
      borderRepository.save(new TerritoryBorder(null, islândia, inglaterra));
      borderRepository.save(new TerritoryBorder(null, islândia, suécia));
      borderRepository.save(new TerritoryBorder(null, inglaterra, suécia));
      borderRepository.save(new TerritoryBorder(null, inglaterra, polônia));
      borderRepository.save(new TerritoryBorder(null, inglaterra, espanha));
      borderRepository.save(new TerritoryBorder(null, suécia, polônia));
      borderRepository.save(new TerritoryBorder(null, suécia, moscou));
      borderRepository.save(new TerritoryBorder(null, polônia, moscou));
      borderRepository.save(new TerritoryBorder(null, polônia, itália));
      borderRepository.save(new TerritoryBorder(null, polônia, espanha));
      borderRepository.save(new TerritoryBorder(null, itália, espanha));
      borderRepository.save(new TerritoryBorder(null, itália, moscou));

      // África - Fronteiras internas
      borderRepository.save(new TerritoryBorder(null, egito, sudão));
      borderRepository.save(new TerritoryBorder(null, egito, nigéria));
      borderRepository.save(new TerritoryBorder(null, nigéria, sudão));
      borderRepository.save(new TerritoryBorder(null, nigéria, congo));
      borderRepository.save(new TerritoryBorder(null, sudão, congo));
      borderRepository.save(new TerritoryBorder(null, sudão, africaSul));
      borderRepository.save(new TerritoryBorder(null, sudão, madagascar));
      borderRepository.save(new TerritoryBorder(null, congo, africaSul));
      borderRepository.save(new TerritoryBorder(null, africaSul, madagascar));

      // Ásia - Fronteiras internas
      borderRepository.save(new TerritoryBorder(null, omsk, dudinka));
      borderRepository.save(new TerritoryBorder(null, omsk, china));
      borderRepository.save(new TerritoryBorder(null, omsk, aral));
      borderRepository.save(new TerritoryBorder(null, dudinka, siberia));
      borderRepository.save(new TerritoryBorder(null, dudinka, tchita));
      borderRepository.save(new TerritoryBorder(null, dudinka, mongólia));
      borderRepository.save(new TerritoryBorder(null, dudinka, china));
      borderRepository.save(new TerritoryBorder(null, siberia, tchita));
      borderRepository.save(new TerritoryBorder(null, siberia, vladivostok));
      borderRepository.save(new TerritoryBorder(null, tchita, vladivostok));
      borderRepository.save(new TerritoryBorder(null, tchita, mongólia));
      borderRepository.save(new TerritoryBorder(null, vladivostok, mongólia));
      borderRepository.save(new TerritoryBorder(null, vladivostok, japao));
      borderRepository.save(new TerritoryBorder(null, mongólia, china));
      borderRepository.save(new TerritoryBorder(null, mongólia, japao));
      borderRepository.save(new TerritoryBorder(null, aral, china));
      borderRepository.save(new TerritoryBorder(null, aral, india));
      borderRepository.save(new TerritoryBorder(null, aral, orienteMedio));
      borderRepository.save(new TerritoryBorder(null, china, india));
      borderRepository.save(new TerritoryBorder(null, china, vietna));
      borderRepository.save(new TerritoryBorder(null, india, vietna));
      borderRepository.save(new TerritoryBorder(null, india, orienteMedio));

      // Oceania - Fronteiras internas
      borderRepository.save(new TerritoryBorder(null, sumatra, australia));
      borderRepository.save(new TerritoryBorder(null, borneo, novaGuine));
      borderRepository.save(new TerritoryBorder(null, borneo, australia));
      borderRepository.save(new TerritoryBorder(null, novaGuine, australia));

      // Fronteiras intercontinentais
      // Oceania - Ásia
      borderRepository.save(new TerritoryBorder(null, borneo, vietna));
      borderRepository.save(new TerritoryBorder(null, sumatra, india));

      // Ásia - Africa
      borderRepository.save(new TerritoryBorder(null, orienteMedio, sudão));
      borderRepository.save(new TerritoryBorder(null, orienteMedio, egito));

      // Ásia - América do Norte
      borderRepository.save(new TerritoryBorder(null, vladivostok, alaska));

      // Ásia - Europa
      borderRepository.save(new TerritoryBorder(null, omsk, moscou));
      borderRepository.save(new TerritoryBorder(null, aral, moscou));
      borderRepository.save(new TerritoryBorder(null, orienteMedio, moscou));
      borderRepository.save(new TerritoryBorder(null, orienteMedio, itália));

      // África - Europa
      borderRepository.save(new TerritoryBorder(null, egito, itália));
      borderRepository.save(new TerritoryBorder(null, nigéria, espanha));

    // África - América do Sul
    borderRepository.save(new TerritoryBorder(null, nigéria, brasil)); bordersCreated++;

    // Europa - América do Norte
    borderRepository.save(new TerritoryBorder(null, islândia, groenlandia)); bordersCreated++;

    // América do Sul - América do Norte
    borderRepository.save(new TerritoryBorder(null, méxico, venezuela)); bordersCreated++;
    
    System.out.println("=== TerritoryInitializer: " + bordersCreated + " bordas criadas com sucesso ===");
  }
}
