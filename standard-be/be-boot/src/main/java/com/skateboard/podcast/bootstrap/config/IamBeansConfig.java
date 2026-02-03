package com.skateboard.podcast.bootstrap.config;

import com.skateboard.podcast.iam.application.service.LoginService;
import com.skateboard.podcast.iam.application.service.RegisterService;
import com.skateboard.podcast.iam.domain.port.out.PasswordHasher;
import com.skateboard.podcast.iam.domain.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.domain.port.out.TokenProvider;
import com.skateboard.podcast.iam.domain.port.out.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IamBeansConfig {

    @Bean
    public RegisterService registerService(
            final UserRepository userRepository,
            final RefreshTokenRepository refreshTokenRepository,
            final PasswordHasher passwordHasher,
            final TokenProvider tokenProvider
    ) {
        return new RegisterService(userRepository, refreshTokenRepository, passwordHasher, tokenProvider);
    }

    @Bean
    public LoginService loginService(
            final UserRepository userRepository,
            final RefreshTokenRepository refreshTokenRepository,
            final PasswordHasher passwordHasher,
            final TokenProvider tokenProvider
    ) {
        return new LoginService(userRepository, refreshTokenRepository, passwordHasher, tokenProvider);
    }
}
