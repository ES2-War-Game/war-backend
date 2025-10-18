package com.war.game.war_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.war.game.war_backend.controller.dto.request.MovementRequestDto;
import com.war.game.war_backend.model.Movement;
import com.war.game.war_backend.security.PlayerDetails;
import com.war.game.war_backend.services.MovementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/movements")
public class MovementController {

    @Autowired
    private MovementService movementService;

    @PostMapping
    public ResponseEntity<Movement> createMovement(
            @Valid @RequestBody MovementRequestDto request,
            @AuthenticationPrincipal PlayerDetails playerDetails) {
        
        Movement movement = movementService.createMovement(request, playerDetails.getPlayer());
        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }

    @DeleteMapping("/{movementId}")
    public ResponseEntity<Void> cancelMovement(
            @PathVariable String movementId,
            @AuthenticationPrincipal PlayerDetails playerDetails) {
        
        movementService.cancelMovement(movementId, playerDetails.getPlayer());
        return ResponseEntity.noContent().build();
    }
}
