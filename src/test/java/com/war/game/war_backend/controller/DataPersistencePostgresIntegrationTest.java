package com.war.game.war_backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.repository.PlayerRepository;

/**
 * Testes de integração que rodam contra uma instância real do Postgres fornecida pelo Testcontainers.
 *
 * Requisito: o Docker precisa estar disponível na máquina que executa os testes.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class DataPersistencePostgresIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void salvarEBuscarPlayer_porId_devePersistirERecuperar() {
        String username = "pg_persist_test_" + System.currentTimeMillis();
        Player p = new Player(username, "persist.pg@example.com", "encodedPass");
        p.setRoles(new HashSet<>());

        Player saved = playerRepository.save(p);

        assertThat(saved.getId()).isNotNull();
        Player found = playerRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getUsername()).isEqualTo(username);
        assertThat(found.getEmail()).isEqualTo("persist.pg@example.com");
        assertThat(found.getRoles()).isNotNull();
    }

    @Test
    void salvarDoisPlayersComMesmoUsername_deveLancarConstraint() {
        String username = "pg_unique_test_" + System.currentTimeMillis();
        Player p1 = new Player(username, "a.pg@example.com", "p1");
        p1.setRoles(new HashSet<>());
        playerRepository.saveAndFlush(p1);

        Player p2 = new Player(username, "b.pg@example.com", "p2");
        p2.setRoles(new HashSet<>());

        assertThrows(DataIntegrityViolationException.class, () -> playerRepository.saveAndFlush(p2));
    }

}
