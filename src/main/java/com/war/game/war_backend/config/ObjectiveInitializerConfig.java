package com.war.game.war_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.war.game.war_backend.model.Objective;
import com.war.game.war_backend.repository.ObjectiveRepository;

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
            
            // Conquistar Continentes:
            Objective obj1 = new Objective();
            obj1.setDescription("Conquistar a América do Sul e a África");
            obj1.setType("CONQUER_CONTINENT");
            objectiveRepository.save(obj1);

            Objective obj2 = new Objective();
            obj2.setDescription("Conquistar a América do Norte e a África");
            obj2.setType("CONQUER_CONTINENT");
            objectiveRepository.save(obj2);

            Objective obj3 = new Objective();
            obj3.setDescription("Conquistar a América do Norte e a Oceania");
            obj3.setType("CONQUER_CONTINENT");
            objectiveRepository.save(obj3);

            Objective obj4 = new Objective();
            obj4.setDescription("Conquistar a Europa, a América do Sul e mais um continente à sua escolha");
            obj4.setType("CONQUER_CONTINENT");
            objectiveRepository.save(obj4);

            Objective obj5 = new Objective();
            obj5.setDescription("Conquistar a Europa, a Oceania e mais um continente à sua escolha");
            obj5.setType("CONQUER_CONTINENT");
            objectiveRepository.save(obj5);

            Objective obj6 = new Objective();
            obj6.setDescription("Conquistar a Europa, a América do Norte e mais um continente à sua escolha");
            obj6.setType("CONQUER_CONTINENT");
            objectiveRepository.save(obj6);

            Objective obj7 = new Objective();
            obj7.setDescription("Conquistar a Ásia e a América do Sul");
            obj7.setType("CONQUER_CONTINENT");
            objectiveRepository.save(obj7);

            Objective obj8 = new Objective();
            obj8.setDescription("Conquistar a Ásia e a África");
            obj8.setType("CONQUER_CONTINENT");
            objectiveRepository.save(obj8);

            Objective obj9 = new Objective();
            obj9.setDescription("Conquistar a Ásia e a Oceania");
            obj9.setType("CONQUER_CONTINENT");
            objectiveRepository.save(obj9);

            // Conquistar Territórios:
            Objective obj10 = new Objective();
            obj10.setDescription("Conquistar 18 territórios com pelo menos 2 exércitos em cada um");
            obj10.setType("CONQUER_TERRITORIES");
            objectiveRepository.save(obj10);

            Objective obj11 = new Objective();
            obj11.setDescription("Conquistar 24 territórios");
            obj11.setType("CONQUER_TERRITORIES");
            objectiveRepository.save(obj11);

            // Eliminar Jogador Específico:
            Objective obj12 = new Objective();
            obj12.setDescription("Eliminar totalmente o jogador com exércitos verdes");
            obj12.setType("ELIMINATE_PLAYER");
            objectiveRepository.save(obj12);

            Objective obj13 = new Objective();
            obj13.setDescription("Eliminar totalmente o jogador com exércitos azuis");
            obj13.setType("ELIMINATE_PLAYER");
            objectiveRepository.save(obj13);

            Objective obj14 = new Objective();
            obj14.setDescription("Eliminar totalmente o jogador com exércitos vermelhos");
            obj14.setType("ELIMINATE_PLAYER");
            objectiveRepository.save(obj14);

            Objective obj15 = new Objective();
            obj15.setDescription("Eliminar totalmente o jogador com exércitos amarelos");
            obj15.setType("ELIMINATE_PLAYER");
            objectiveRepository.save(obj15);

            Objective obj16 = new Objective();
            obj16.setDescription("Eliminar totalmente o jogador com exércitos pretos");
            obj16.setType("ELIMINATE_PLAYER");
            objectiveRepository.save(obj16);

            Objective obj17 = new Objective();
            obj17.setDescription("Eliminar totalmente o jogador com exércitos roxos");
            obj17.setType("ELIMINATE_PLAYER");
            objectiveRepository.save(obj17);
            
            System.out.println("=== ObjectiveInitializer: Finalizado! Total de objetivos criados: 18 ===");
        };
    }
}
