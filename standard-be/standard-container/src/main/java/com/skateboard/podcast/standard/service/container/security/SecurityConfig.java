package com.skateboard.podcast.standard.service.container.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final List<String> corsAllowedOrigins;

    public SecurityConfig(
            final JwtAuthenticationFilter jwtFilter,
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}") final String corsAllowedOrigins
    ) {
        this.jwtFilter = jwtFilter;
        this.corsAllowedOrigins = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/feed").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/events").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/events/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/public/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/public/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/public/auth/admin-passcode").permitAll()
                        .requestMatchers(HttpMethod.POST, "/public/auth/social").permitAll()
                        .requestMatchers(HttpMethod.POST, "/public/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/app-config").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        // admin endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // everything else requires auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsAllowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        config.setExposedHeaders(List.of("Location"));
        config.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
