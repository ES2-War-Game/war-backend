package com.war.game.war_backend.config;

import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.model.TerritoryBorder;
import com.war.game.war_backend.repository.TerritoryRepository;
import com.war.game.war_backend.repository.TerritoryBorderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializerConfig {  
  @Bean
  public CommandLineRunner dataInitializer(TerritoryRepository territoryRepository,
      TerritoryBorderRepository borderRepository) {
    return args -> {
      // América do Norte
      Territory alaska = territoryRepository.save(new Territory(null, "ALASKA", "América do Norte", null, null));
      Territory mackenzie = territoryRepository.save(new Territory(null, "MACKENZIE", "América do Norte", null, null));
      Territory vancouver = territoryRepository.save(new Territory(null, "VANCOUVER", "América do Norte", null, null));
      Territory ottawa = territoryRepository.save(new Territory(null, "OTTAWA", "América do Norte", null, null));
      Territory labrador = territoryRepository.save(new Territory(null, "LABRADOR", "América do Norte", null, null));
      Territory califórnia = territoryRepository
          .save(new Territory(null, "CALIFÓRNIA", "América do Norte", null, null));
      Territory novaYork = territoryRepository.save(new Territory(null, "NOVA YORK", "América do Norte", null, null));
      Territory méxico = territoryRepository.save(new Territory(null, "MÉXICO", "América do Norte", null, null));
      Territory groenlandia = territoryRepository
          .save(new Territory(null, "GROENLÂNDIA", "América do Norte", null, null));

      // América do Sul
      Territory venezuela = territoryRepository.save(new Territory(null, "VENEZUELA", "América do Sul", null, null));
      Territory brasil = territoryRepository.save(new Territory(null, "BRASIL", "América do Sul", null, null));
      Territory bolivia = territoryRepository.save(new Territory(null, "BOLÍVIA", "América do Sul", null, null));
      Territory argentina = territoryRepository.save(new Territory(null, "ARGENTINA", "América do Sul", null, null));

      // Europa
      Territory islândia = territoryRepository.save(new Territory(null, "ISLÂNDIA", "Europa", null, null));
      Territory inglaterra = territoryRepository.save(new Territory(null, "INGLATERRA", "Europa", null, null));
      Territory suécia = territoryRepository.save(new Territory(null, "SUÉCIA", "Europa", null, null));
      Territory polônia = territoryRepository.save(new Territory(null, "POLÔNIA", "Europa", null, null));
      Territory itália = territoryRepository.save(new Territory(null, "ITÁLIA", "Europa", null, null));
      Territory espanha = territoryRepository.save(new Territory(null, "ESPANHA", "Europa", null, null));
      Territory moscou = territoryRepository.save(new Territory(null, "MOSCOU", "Europa", null, null));

      // África
      Territory egito = territoryRepository.save(new Territory(null, "EGITO", "África", null, null));
      Territory nigéria = territoryRepository.save(new Territory(null, "NIGÉRIA", "África", null, null));
      Territory sudão = territoryRepository.save(new Territory(null, "SUDÃO", "África", null, null));
      Territory congo = territoryRepository.save(new Territory(null, "CONGO", "África", null, null));
      Territory africaSul = territoryRepository.save(new Territory(null, "ÁFRICA DO SUL", "África", null, null));
      Territory madagascar = territoryRepository.save(new Territory(null, "MADAGASCAR", "África", null, null));

      // Ásia
      Territory omsk = territoryRepository.save(new Territory(null, "OMSK", "Ásia", null, null));
      Territory dudinka = territoryRepository.save(new Territory(null, "DUDINKA", "Ásia", null, null));
      Territory siberia = territoryRepository.save(new Territory(null, "SIBÉRIA", "Ásia", null, null));
      Territory vladivostok = territoryRepository.save(new Territory(null, "VLADIVOSTOK", "Ásia", null, null));
      Territory tchita = territoryRepository.save(new Territory(null, "TCHITA", "Ásia", null, null));
      Territory mongólia = territoryRepository.save(new Territory(null, "MONGÓLIA", "Ásia", null, null));
      Territory japao = territoryRepository.save(new Territory(null, "JAPÃO", "Ásia", null, null));
      Territory aral = territoryRepository.save(new Territory(null, "ARAL", "Ásia", null, null));
      Territory china = territoryRepository.save(new Territory(null, "CHINA", "Ásia", null, null));
      Territory india = territoryRepository.save(new Territory(null, "ÍNDIA", "Ásia", null, null));
      Territory vietna = territoryRepository.save(new Territory(null, "VIETNÃ", "Ásia", null, null));
      Territory orienteMedio = territoryRepository.save(new Territory(null, "ORIENTE MÉDIO", "Ásia", null, null));

      // Oceania
      Territory australia = territoryRepository.save(new Territory(null, "AUSTRÁLIA", "Oceania", null, null));
      Territory sumatra = territoryRepository.save(new Territory(null, "SUMATRA", "Oceania", null, null));
      Territory borneo = territoryRepository.save(new Territory(null, "BORNEO", "Oceania", null, null));
      Territory novaGuine = territoryRepository.save(new Territory(null, "NOVA GUINÉ", "Oceania", null, null));

      // Fronteiras conforme o mapa do War

      // América do Norte - Fronteiras internas
      borderRepository.save(new TerritoryBorder(null, alaska, mackenzie));
      borderRepository.save(new TerritoryBorder(null, alaska, vancouver));
      borderRepository.save(new TerritoryBorder(null, mackenzie, vancouver));
      borderRepository.save(new TerritoryBorder(null, mackenzie, ottawa));
      borderRepository.save(new TerritoryBorder(null, mackenzie, groenlandia));
      borderRepository.save(new TerritoryBorder(null, vancouver, ottawa));
      borderRepository.save(new TerritoryBorder(null, vancouver, califórnia));
      borderRepository.save(new TerritoryBorder(null, ottawa, labrador));
      borderRepository.save(new TerritoryBorder(null, ottawa, novaYork));
      borderRepository.save(new TerritoryBorder(null, ottawa, califórnia));
      borderRepository.save(new TerritoryBorder(null, ottawa, groenlandia));
      borderRepository.save(new TerritoryBorder(null, labrador, groenlandia));
      borderRepository.save(new TerritoryBorder(null, labrador, novaYork));
      borderRepository.save(new TerritoryBorder(null, califórnia, novaYork));
      borderRepository.save(new TerritoryBorder(null, califórnia, méxico));
      borderRepository.save(new TerritoryBorder(null, novaYork, méxico));

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
      borderRepository.save(new TerritoryBorder(null, nigéria, brasil));

      // Europa - América do Norte
      borderRepository.save(new TerritoryBorder(null, islândia, groenlandia));

      // América do Sul - América do Norte
      borderRepository.save(new TerritoryBorder(null, méxico, venezuela));
    };
  }
}
