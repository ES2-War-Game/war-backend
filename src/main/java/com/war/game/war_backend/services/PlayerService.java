package com.war.game.war_backend.services;

import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.Role;
import com.war.game.war_backend.repository.PlayerRepository;
import com.war.game.war_backend.repository.RoleRepository;
import com.war.game.war_backend.controller.dto.request.PlayerRegistrationDto;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Service
public class PlayerService {

  private final PlayerRepository playerRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public PlayerService(PlayerRepository playerRepository, RoleRepository roleRepository,
      PasswordEncoder passwordEncoder) {
    this.playerRepository = playerRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Player registerNewPlayer(PlayerRegistrationDto registrationDto) {
    if (playerRepository.findByUsername(registrationDto.getUsername()).isPresent() ||
        playerRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
      throw new IllegalStateException("Nome de usuario ou email ja existe!");
    }

    // role padrão é "ROLE_USER"
    Role defaultRole = roleRepository.findByName("ROLE_USER")
        .orElseThrow(() -> new IllegalStateException("Role padrão não encontrada."));

    Set<Role> roles = new HashSet<>();
    roles.add(defaultRole);

    Player newPlayer = new Player();
    newPlayer.setUsername(registrationDto.getUsername());
    newPlayer.setEmail(registrationDto.getEmail());

    String hashedPassword = passwordEncoder.encode(registrationDto.getPassword());
    newPlayer.setPassword(hashedPassword);

    newPlayer.setRoles(roles);

    return playerRepository.save(newPlayer);
  }

  public Player getPlayerByUsername(String username) {
        return playerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado."));
  }
}