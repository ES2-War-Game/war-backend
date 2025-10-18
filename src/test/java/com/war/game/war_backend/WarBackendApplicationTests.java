package com.war.game.war_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.war.game.war_backend.config.TestRedisConfig;

@SpringBootTest(classes = {WarBackendApplication.class, TestRedisConfig.class})
@ActiveProfiles("test")
class WarBackendApplicationTests {
  @Test
  void contextLoads() {
  }
}
