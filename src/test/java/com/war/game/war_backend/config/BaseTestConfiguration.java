package com.war.game.war_backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

@TestConfiguration
@ActiveProfiles("test")
@Import({TestRedisConfig.class})
@EnableAutoConfiguration(exclude = {RedisAutoConfiguration.class})
@PropertySource("classpath:application-test.properties")
public class BaseTestConfiguration {
    // Common test configuration goes here
}