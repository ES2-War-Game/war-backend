package com.war.game.war_backend.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.war.game.war_backend.controller.dto.response.TroopMovementResponse;

@Service
public class TroopMovementWebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyTroopMovementUpdate(Long gameId, TroopMovementResponse movement) {
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/troop-movements", movement);
    }

    public void notifyTroopMovementComplete(Long gameId, TroopMovementResponse movement) {
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/troop-movements/complete", movement);
    }
}
