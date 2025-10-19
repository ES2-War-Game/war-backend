package com.war.game.war_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.war.game.war_backend.services.MovementService;

@Component
public class MovementExpirationListener implements MessageListener {
    
    @Autowired
    private MovementService movementService;

    private static final String MOVEMENT_PREFIX = "movement:";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());
        if (expiredKey.startsWith(MOVEMENT_PREFIX)) {
            String movementId = expiredKey.substring(MOVEMENT_PREFIX.length());
            movementService.completeMovement(movementId);
        }
    }
}
