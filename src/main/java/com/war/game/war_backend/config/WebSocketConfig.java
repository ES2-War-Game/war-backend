package com.war.game.war_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Define o prefixo para o destino das mensagens enviadas do servidor para o cliente
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Define o prefixo para o destino das mensagens enviadas do cliente para o servidor
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para a conexão WebSocket 'ws://localhost:8080/ws'
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }
}
