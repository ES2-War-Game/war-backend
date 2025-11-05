package com.war.game.war_backend.controller.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      // keep first error per field
      errors.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }

  @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, String>> handleMissingParameterException(
      org.springframework.web.bind.MissingServletRequestParameterException ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put(ex.getParameterName(), "Este parâmetro é obrigatório");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }

  // Fallback generic handler (optional) to return a concise JSON message instead of a stacktrace
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
    Map<String, String> body = new HashMap<>();
    body.put("error", ex.getMessage() != null ? ex.getMessage() : "unexpected error");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
