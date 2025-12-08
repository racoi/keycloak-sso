package com.example.bservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final HandlerMappingIntrospector introspector;

    public SecurityConfig(
        ClientRegistrationRepository clientRegistrationRepository,
        HandlerMappingIntrospector introspector
    ) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.introspector = introspector;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login/**").permitAll()
                .requestMatchers("/logout/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                // API 미인증 시 401 반환 → 프론트 JS가 prompt=none 리다이렉트 처리
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new MvcRequestMatcher(introspector, "/api/**")
                )
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/", true)
                .failureHandler((req, res, ex) -> res.sendRedirect("/?login_required=1"))
                .authorizationEndpoint(auth -> auth
                    .authorizationRequestResolver(authorizationRequestResolver())
                )
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("http://localhost:8090/realms/myrealm/protocol/openid-connect/logout?post_logout_redirect_uri=http://localhost:8082/&client_id=b-client")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );
        
        return http.build();
    }

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
            );

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return customize(defaultResolver.resolve(request), request);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                return customize(defaultResolver.resolve(request, clientRegistrationId), request);
            }

            private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
                if (authorizationRequest == null) {
                    return null;
                }

                String prompt = request.getParameter("prompt");
                if (StringUtils.hasText(prompt)) {
                    Map<String, Object> extra = new HashMap<>(authorizationRequest.getAdditionalParameters());
                    extra.put("prompt", prompt);
                    return OAuth2AuthorizationRequest
                        .from(authorizationRequest)
                        .additionalParameters(extra)
                        .build();
                }

                return authorizationRequest;
            }
        };
    }

}
