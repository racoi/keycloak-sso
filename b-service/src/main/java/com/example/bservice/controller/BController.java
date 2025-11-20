package com.example.bservice.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class BController {

    @GetMapping("/")
    public Map<String, Object> home(@AuthenticationPrincipal OidcUser oidcUser) {

        String username = oidcUser != null ? oidcUser.getAttribute("preferred_username") : "unknown";
        String email = oidcUser != null ? oidcUser.getAttribute("email") : "unknown";
        String token = oidcUser != null ? oidcUser.getIdToken().getTokenValue() : "unknown";

        return Map.of(
                "service", "a-service",
                "user", username,
                "email", email,
                "token", token
        );
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of(
                "message", "Logout successful.",
                "logoutUrl", "/logout"
        );
    }
}