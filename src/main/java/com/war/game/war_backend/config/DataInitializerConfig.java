package com.war.game.war_backend.config;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.GameTerritory;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.model.Role;
import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.model.enums.GameConstants;
import com.war.game.war_backend.model.enums.GameStatus;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.GameTerritoryRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import com.war.game.war_backend.repository.PlayerRepository;
import com.war.game.war_backend.repository.RoleRepository;
import com.war.game.war_backend.repository.TerritoryRepository;

@Configuration
public class DataInitializerConfig {

  @Bean
  @Order(2) // Executa depois do TerritoryInitializerConfig
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
      Role userRole =
          roleRepository
              .findByName("ROLE_USER")
              .orElseGet(
                  () -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                  });

      // Create test user if it doesn't exist
      if (playerRepository.findByUsername("testuser").isEmpty()) {

        // 1. Create player
        Player player =
            new Player("testuser", "test@example.com", passwordEncoder.encode("test123"));

        // Garantindo que o campo imageUrl (se for not null no Player) seja setado
        player.setImageUrl("https://via.placeholder.com/150/ff0000?text=T");

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        player.setRoles(roles);
        player = playerRepository.save(player);

        // 2. Create a new game for testing
        Game game = new Game();
        game.setName("Test Game");
        game.setStatus(GameStatus.IN_PROGRESS.name());
        game.setCreatedAt(LocalDateTime.now());
        game.setCardSetExchangeCount(0);
        game = gameRepository.save(game);

        // 3. Create PlayerGame entry
        PlayerGame playerGame = new PlayerGame();
        playerGame.setGame(game);
        playerGame.setPlayer(player);

        playerGame.setUsername(player.getUsername());
        playerGame.setImageUrl(player.getImageUrl());
        playerGame.setColor(GameConstants.AVAILABLE_COLORS.get(0));
        // -------------------------------------

        playerGame.setIsOwner(true);
        playerGame.setStillInGame(true);

        playerGame = playerGameRepository.save(playerGame);

        // 4. Add some initial territories with armies
        Territory territory1 =
            territoryRepository
                .findByName("BRASIL")
                .orElseThrow(() -> new RuntimeException("Territory 'BRASIL' not found"));
        Territory territory2 =
            territoryRepository
                .findByName("ARGENTINA")
                .orElseThrow(() -> new RuntimeException("Territory 'ARGENTINA' not found"));

        // 5. Create GameTerritory entries with initial armies
        GameTerritory gameTerritory1 = new GameTerritory();
        gameTerritory1.setTerritory(territory1);
        gameTerritory1.setGame(game);
        gameTerritory1.setOwner(playerGame);
        gameTerritory1.setStaticArmies(10); // Todas as tropas iniciais são estáticas
        gameTerritory1.setMovedInArmies(0); // Nenhuma tropa movida inicialmente
        gameTerritory1.setUnallocatedArmies(0); // Nenhuma tropa não alocada
        gameTerritoryRepository.save(gameTerritory1);

        GameTerritory gameTerritory2 = new GameTerritory();
        gameTerritory2.setTerritory(territory2);
        gameTerritory2.setGame(game);
        gameTerritory2.setOwner(playerGame);
        gameTerritory2.setStaticArmies(5); // Todas as tropas iniciais são estáticas
        gameTerritory2.setMovedInArmies(0); // Nenhuma tropa movida inicialmente
        gameTerritory2.setUnallocatedArmies(0); // Nenhuma tropa não alocada
        gameTerritoryRepository.save(gameTerritory2);
      }
    };
  }
}
