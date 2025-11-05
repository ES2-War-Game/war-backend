package com.war.game.war_backend.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Test-friendly JwtTokenUtil used only in tests (placed under src/test so it overrides the
 * production implementation on the test classpath). It produces simple tokens of the form
 * "test-token-{username}" and validates/extracts usernames accordingly.
 */
@Component
public class JwtTokenUtil {

    public String generateToken(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) return null;
        return "test-token-" + userDetails.getUsername();
    }

    public String getUsernameFromToken(String token) {
        if (token == null) return null;
        if (token.startsWith("test-token-")) return token.substring("test-token-".length());
        return null;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return username != null
                && userDetails != null
                && username.equals(userDetails.getUsername());
    }
}
