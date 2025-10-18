package com.war.game.war_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.war.game.war_backend.controller.dto.request.TroopMovementRequest;
import com.war.game.war_backend.controller.dto.response.TroopMovementResponse;
import com.war.game.war_backend.security.PlayerDetails;
import com.war.game.war_backend.service.TroopMovementService;

@RestController
@RequestMapping("/api/troop-movements")
public class TroopMovementController {

    @Autowired
    private TroopMovementService troopMovementService;

    @PostMapping
    public ResponseEntity<TroopMovementResponse> createTroopMovement(
            @AuthenticationPrincipal PlayerDetails playerDetails,
            @RequestBody TroopMovementRequest request) {
        return ResponseEntity.ok(troopMovementService.createTroopMovement(playerDetails.getId(), request));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<TroopMovementResponse>> getTroopMovements(@PathVariable Long gameId) {
        return ResponseEntity.ok(troopMovementService.getTroopMovementsByGame(gameId));
    }
}
