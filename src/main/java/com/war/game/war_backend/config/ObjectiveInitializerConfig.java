package com.war.game.war_backend.config;

import com.war.game.war_backend.model.Objective;
import com.war.game.war_backend.repository.ObjectiveRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class ObjectiveInitializerConfig {
    
    @Bean
    @Order(3) // Executa por último
    public CommandLineRunner objectiveInitializer(ObjectiveRepository objectiveRepository) {
        return args -> {
            System.out.println("=== ObjectiveInitializer: Iniciando verificação ===");
            
            long objectiveCount = objectiveRepository.count();
            System.out.println("=== ObjectiveInitializer: Objetivos existentes: " + objectiveCount + " ===");
            
            if (objectiveCount > 0) {
                System.out.println("=== ObjectiveInitializer: Objetivos já existem, pulando inicialização ===");
                return;
            }

            System.out.println("=== ObjectiveInitializer: Criando objetivos do jogo ===");
            
            objectiveRepository.saveAll(List.of(
                // Conquistar Continentes:
                new Objective(null, "Conquistar a América do Sul e a África", "CONQUER_CONTINENT"),
                new Objective(null, "Conquistar a América do Norte e a África", "CONQUER_CONTINENT"),
                new Objective(null, "Conquistar a América do Norte e a Oceania", "CONQUER_CONTINENT"),
                new Objective(null, "Conquistar a Europa, a América do Sul e mais um continente à sua escolha",
                        "CONQUER_CONTINENT"),
                new Objective(null, "Conquistar a Europa, a Oceania e mais um continente à sua escolha",
                        "CONQUER_CONTINENT"),
                new Objective(null, "Conquistar a Europa, a América do Norte e mais um continente à sua escolha",
                        "CONQUER_CONTINENT"),
                new Objective(null, "Conquistar a Ásia e a América do Sul", "CONQUER_CONTINENT"),
                new Objective(null, "Conquistar a Ásia e a África", "CONQUER_CONTINENT"),
                new Objective(null, "Conquistar a Ásia e a Oceania", "CONQUER_CONTINENT"),

                // Conquistar Territórios:
                new Objective(null, "Conquistar 18 territórios com pelo menos 2 exércitos em cada um",
                        "CONQUER_TERRITORIES"),
                new Objective(null, "Conquistar 24 territórios", "CONQUER_TERRITORIES"),

                // Eliminar Jogador Específico:
                new Objective(null, "Eliminar totalmente o jogador com exércitos verdes", "ELIMINATE_PLAYER"),
                new Objective(null, "Eliminar totalmente o jogador com exércitos azuis", "ELIMINATE_PLAYER"),
                new Objective(null, "Eliminar totalmente o jogador com exércitos vermelhos", "ELIMINATE_PLAYER"),
                new Objective(null, "Eliminar totalmente o jogador com exércitos amarelos", "ELIMINATE_PLAYER"),
                new Objective(null, "Eliminar totalmente o jogador com exércitos pretos", "ELIMINATE_PLAYER"),
                new Objective(null, "Eliminar totalmente o jogador com exércitos rosas", "ELIMINATE_PLAYER")
            ));
            
            System.out.println("=== ObjectiveInitializer: Finalizado! Total de objetivos criados: 18 ===");
        };
    }
}
