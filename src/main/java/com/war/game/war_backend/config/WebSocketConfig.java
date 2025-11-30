package com.war.game.war_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.war.game.war_backend.security.websocket.JwtChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final JwtChannelInterceptor jwtChannelInterceptor;

  // Injeção de Dependência do Interceptor
  @Autowired
  public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor) {
    this.jwtChannelInterceptor = jwtChannelInterceptor;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // Define o prefixo para o destino das mensagens enviadas do servidor para o
    // cliente
    registry.enableSimpleBroker("/topic", "/queue");

    // Define o prefixo para o destino das mensagens enviadas do cliente para o
    // servidor
    registry.setApplicationDestinationPrefixes("/app");
  }

  // REGISTRO DO INTERCEPTOR DE CANAL AQUI
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    // Adiciona o interceptor para processar a autenticação de mensagens de entrada
    // (incluindo CONNECT)
    registration.interceptors(jwtChannelInterceptor);
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // MELHORIA: Restringir allowedOriginPatterns ao invés de '*'
    registry
        .addEndpoint("/ws")
        // Use as origens permitidas no seu SecurityConfig para maior segurança
        .setAllowedOriginPatterns("http://localhost:5173", "http://localhost:3000", "https://war-frontend-ten.vercel.app/")
        .withSockJS();
  }
}
