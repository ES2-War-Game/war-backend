package com.war.game.war_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.war.game.war_backend.controller.dto.response.HealthCheckResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Endpoint para verificação de saúde da aplicação")
public class HealthController {

  private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public HealthController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @GetMapping
  @Operation(
      summary = "Verifica a saúde da aplicação",
      description =
          "Verifica a conexão com o banco de dados e retorna métricas básicas da aplicação")
  @ApiResponse(responseCode = "200", description = "Aplicação está saudável")
  @ApiResponse(responseCode = "500", description = "Problemas internos detectados")
  public ResponseEntity<?> healthCheck() {
    long startTime = System.currentTimeMillis();

    try {
      // Testa a conexão com o banco de dados
      jdbcTemplate.queryForObject("SELECT 1", Integer.class);

      HealthCheckResponse response = HealthCheckResponse.ok(System.currentTimeMillis() - startTime);

      logger.info(
          "Healthcheck successful - uptime: {} seconds, duration: {} ms",
          response.getUptime(),
          response.getDuration());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;

      logger.error("Healthcheck failed - duration: {} ms - error: {}", duration, e.getMessage());

      return ResponseEntity.status(500)
          .body(HealthCheckResponse.error("Internal healthcheck error"));
    }
  }
}
