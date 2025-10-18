package com.war.game.war_backend.security;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.war.game.war_backend.model.Player;

public class PlayerDetails implements UserDetails {

  private final Player player;

  public PlayerDetails(Player player) {
    this.player = player;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return player.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority(role.getName()))
        .collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return player.getPassword();
  }

  @Override
  public String getUsername() {
    return player.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  public Player getPlayer() {
    return player;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public Long getId() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getId'");
  }
}
