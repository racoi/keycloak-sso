package com.example.aservice.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AController {

    @GetMapping("/user")
    public Map<String, Object> home(@AuthenticationPrincipal OidcUser oidcUser,
                                  @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient client) {

        String username = oidcUser != null ? oidcUser.getAttribute("preferred_username") : "unknown";
        String email = oidcUser != null ? oidcUser.getAttribute("email") : "unknown";
        String idToken = oidcUser != null ? oidcUser.getIdToken().getTokenValue() : "unknown";
        String accessToken = client != null ? client.getAccessToken().getTokenValue() : "unknown";
        String refreshToken = (client != null && client.getRefreshToken() != null) ? client.getRefreshToken().getTokenValue() : "unknown";

        return Map.of(
                "service", "a-service",
                "user", username,
                "email", email,
                "idToken", idToken,
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }
}