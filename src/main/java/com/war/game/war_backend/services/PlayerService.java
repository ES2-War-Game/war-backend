package com.war.game.war_backend.services;

import com.war.game.war_backend.controller.dto.request.PlayerRegistrationDto;
import com.war.game.war_backend.controller.dto.request.PlayerUpdateDto;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.Role;
import com.war.game.war_backend.repository.PlayerRepository;
import com.war.game.war_backend.repository.RoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PlayerService(
            PlayerRepository playerRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Player registerNewPlayer(PlayerRegistrationDto registrationDto) {
        if (playerRepository.findByUsername(registrationDto.getUsername()).isPresent()
                || playerRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new IllegalStateException("Nome de usuario ou email ja existe!");
        }

        // role padrão é "ROLE_USER"
        Role defaultRole =
                roleRepository
                        .findByName("ROLE_USER")
                        .orElseThrow(
                                () -> new IllegalStateException("Role padrão não encontrada."));

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
        return playerRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado."));
    }

    public Player getPlayerById(Long id) {
        return playerRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado."));
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player updatePlayer(Long id, PlayerUpdateDto updateDto) {
        Player player = getPlayerById(id);
        if (updateDto.getEmail() != null) player.setEmail(updateDto.getEmail());
        if (updateDto.getImageUrl() != null) player.setImageUrl(updateDto.getImageUrl());
        return playerRepository.save(player);
    }
}
