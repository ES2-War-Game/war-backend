package com.war.game.war_backend.security.websocket;

import com.war.game.war_backend.security.CustomUserDetailsService;
import com.war.game.war_backend.security.jwt.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtChannelInterceptor.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public JwtChannelInterceptor(
            JwtTokenUtil jwtTokenUtil, CustomUserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Obtém o cabeçalho Authorization
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
                String jwtToken = authorizationHeader.substring(BEARER_PREFIX.length());

                try {
                    String username = jwtTokenUtil.getUsernameFromToken(jwtToken);

                    if (username != null) {
                        // 1. Carrega os detalhes do usuário
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // 2. Chama validateToken com os argumentos CORRETOS: (String, UserDetails)
                        if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {

                            // Cria o objeto de autenticação
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());

                            // Define o usuário autenticado na sessão WebSocket
                            accessor.setUser(authentication);

                            logger.info("STOMP CONNECT: Usuário autenticado: {}", username);
                        } else {
                            logger.warn("STOMP CONNECT: Token JWT inválido ou expirado.");
                        }
                    } else {
                        logger.warn("STOMP CONNECT: Username nulo no token.");
                    }
                } catch (UsernameNotFoundException e) {
                    logger.warn("STOMP CONNECT: Usuário não encontrado no banco de dados.");
                } catch (Exception e) {
                    logger.warn(
                            "STOMP CONNECT: Erro de processamento/validação de token: {}",
                            e.getMessage());
                }
            } else {
                logger.warn(
                        "STOMP CONNECT: Cabeçalho de autorização não encontrado ou formato incorreto.");
            }
        }

        return message;
    }
}
