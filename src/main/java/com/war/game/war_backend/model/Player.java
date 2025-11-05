package com.war.game.war_backend.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "player")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
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

  // Relacionamento muitos pra muitos com Role
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "player_role",
      joinColumns = @JoinColumn(name = "player_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @EqualsAndHashCode.Exclude
  private Set<Role> roles;

  // Relacionamento com PlayerGame
  @OneToMany(mappedBy = "player")
  @JsonIgnore
  @EqualsAndHashCode.Exclude
  private Set<PlayerGame> playerGames;

  public Player(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }
}
