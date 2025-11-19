package com.example.bservice.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class BController {

    @GetMapping("/")
    public Map<String, Object> home(@AuthenticationPrincipal OAuth2User oauth2User) {
        String username = oauth2User != null ? oauth2User.getAttribute("preferred_username") : "unknown";
        String email = oauth2User != null ? oauth2User.getAttribute("email") : "unknown";
        
        return Map.of(
            "service", "b-service",
            "user", username,
            "email", email
        );
    }
}
