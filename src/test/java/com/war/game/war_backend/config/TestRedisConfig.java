package com.war.game.war_backend.config;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.war.game.war_backend.model.Movement;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
public class TestRedisConfig {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        RedisConnectionFactory mockFactory = mock(RedisConnectionFactory.class);
        RedisConnection mockConnection = mock(RedisConnection.class, RETURNS_DEEP_STUBS);
        StringRedisConnection mockStringConnection = mock(StringRedisConnection.class);

        when(mockFactory.getConnection()).thenReturn(mockConnection);
        when(mockConnection.stringCommands()).thenReturn(mock(RedisStringCommands.class));

        // Add more mock behaviors as needed for your tests
        doNothing().when(mockConnection).close();
        when(mockConnection.isClosed()).thenReturn(false);

        return mockFactory;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Movement> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Movement> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Movement.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Movement.class));
        template.setEnableDefaultSerializer(false);
        template.afterPropertiesSet();
        return template;
    }
}
