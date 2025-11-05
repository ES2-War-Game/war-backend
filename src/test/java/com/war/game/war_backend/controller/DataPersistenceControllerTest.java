package com.war.game.war_backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.repository.PlayerRepository;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de persistência básicos para garantir que o banco H2 (profile test) está funcionando: CRUD
 * básico para a entidade Player e verificação de constraint de unicidade no username.
 *
 * <p>Observações: - Usa o profile "test" que inicializa H2 em memória. - Cada teste roda dentro de
 * uma transação que será rollbackada ao final.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DataPersistenceControllerTest {

    @Autowired private PlayerRepository userRepository;

    @Test
    void salvarEBuscarPlayer_porId_devePersistirERecuperar() {
        String username = "persist_test_" + System.currentTimeMillis();
        Player p = new Player(username, "persist@example.com", "encodedPass");
        p.setRoles(new HashSet<>());

        Player saved = userRepository.save(p);

        assertThat(saved.getId()).isNotNull();
        Player found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getUsername()).isEqualTo(username);
        assertThat(found.getEmail()).isEqualTo("persist@example.com");
        assertThat(found.getRoles()).isNotNull();
    }

    @Test
    void salvarDoisPlayersComMesmoUsername_deveLancarConstraint() {
        String username = "unique_test_" + System.currentTimeMillis();
        Player p1 = new Player(username, "a@example.com", "p1");
        p1.setRoles(new HashSet<>());
        userRepository.saveAndFlush(p1);

        // Segundo player com mesmo username - espera violação de constraint do DB
        Player p2 = new Player(username, "b@example.com", "p2");
        p2.setRoles(new HashSet<>());

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(p2));
    }
}
