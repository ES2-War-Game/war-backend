package com.war.game.war_backend.config;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.model.Role;
import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.GameTerritoryRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.PlayerRepository;
import com.war.game.war_backend.repository.RoleRepository;
import com.war.game.war_backend.repository.TerritoryRepository;

@Configuration
public class DataInitializerConfig {

    @Bean
    CommandLineRunner initDatabase(
            PlayerRepository playerRepository,
            RoleRepository roleRepository,
            TerritoryRepository territoryRepository,
            GameTerritoryRepository gameTerritoryRepository,
            GameRepository gameRepository,
            PlayerGameRepository playerGameRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Create default role if it doesn't exist
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName("ROLE_USER");
                        return roleRepository.save(newRole);
                    });

            // Create test user if it doesn't exist
            if (playerRepository.findByUsername("testuser").isEmpty()) {
                // Create player
                Player player = new Player("testuser", "test@example.com", passwordEncoder.encode("test123"));
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                player.setRoles(roles);
                player = playerRepository.save(player);

                // Create a new game for testing
                Game game = new Game();
                game.setStatus("ACTIVE");
                game.setCreatedAt(LocalDateTime.now());
                game = gameRepository.save(game);

                // Create PlayerGame entry
                PlayerGame playerGame = new PlayerGame();
                playerGame.setGame(game);
                playerGame.setPlayer(player);
                playerGame.setColor("RED");
                playerGame = playerGameRepository.save(playerGame);
                
                // Add some initial territories with armies
                Territory territory1 = territoryRepository.findByName("Brasil")
                    .orElseThrow(() -> new RuntimeException("Territory 'Brasil' not found"));
                Territory territory2 = territoryRepository.findByName("Argentina")
                    .orElseThrow(() -> new RuntimeException("Territory 'Argentina' not found"));

                // Create GameTerritory entries with initial armies
                GameTerritory gameTerritory1 = new GameTerritory();
                gameTerritory1.setTerritory(territory1);
                gameTerritory1.setGame(game);
                gameTerritory1.setOwner(playerGame);
                gameTerritory1.setArmies(10);
                gameTerritoryRepository.save(gameTerritory1);

                GameTerritory gameTerritory2 = new GameTerritory();
                gameTerritory2.setTerritory(territory2);
                gameTerritory2.setGame(game);
                gameTerritory2.setOwner(playerGame);
                gameTerritory2.setArmies(5);
                gameTerritoryRepository.save(gameTerritory2);
            }
        };
    }
}
