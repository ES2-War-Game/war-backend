package com.war.game.war_backend.exceptions;

public class UsernameConflictException extends RuntimeException {
  public UsernameConflictException(String message) {
    super(message);
  }
}
