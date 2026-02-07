package com.skateboard.podcast.standard.service.container.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(final JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
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
                        .requestMatchers(HttpMethod.POST, "/public/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/public/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/public/auth/admin-passcode").permitAll()
                        .requestMatchers(HttpMethod.POST, "/public/auth/social").permitAll()
                        .requestMatchers(HttpMethod.POST, "/public/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/app-config").permitAll()

                        // admin endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // everything else requires auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
