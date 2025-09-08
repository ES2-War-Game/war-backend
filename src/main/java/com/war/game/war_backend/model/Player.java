package com.war.game.war_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "player")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk_id")
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String password;

  @Column(name = "image_url", nullable = true, length = 255)
  private String imageUrl;

  // Relacionamento muitos pra muitos com Role via PlayerRole
  @ManyToMany
  @JoinTable(name = "player_role", joinColumns = @JoinColumn(name = "player_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles;

  // Relacionamento com PlayerGame
  @OneToMany(mappedBy = "player")
  private Set<PlayerGame> playerGames;

  public Player(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }
}