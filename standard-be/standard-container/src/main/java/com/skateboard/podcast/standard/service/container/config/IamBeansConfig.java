package com.skateboard.podcast.standard.service.container.config;

import com.skateboard.podcast.iam.service.application.service.LoginService;
import com.skateboard.podcast.iam.service.application.service.LogoutService;
import com.skateboard.podcast.iam.service.application.service.RefreshService;
import com.skateboard.podcast.iam.service.application.service.RegisterService;
import com.skateboard.podcast.iam.service.application.port.in.LoginUseCase;
import com.skateboard.podcast.iam.service.application.port.in.LogoutUseCase;
import com.skateboard.podcast.iam.service.application.port.in.RefreshUseCase;
import com.skateboard.podcast.iam.service.application.port.in.RegisterUseCase;
import com.skateboard.podcast.iam.service.application.port.out.PasswordHasher;
import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.service.application.port.out.TokenProvider;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IamBeansConfig {

    @Bean
    public RegisterUseCase registerService(
            final UserRepository userRepository,
            final RefreshTokenRepository refreshTokenRepository,
            final PasswordHasher passwordHasher,
            final TokenProvider tokenProvider
    ) {
        return new RegisterService(userRepository, refreshTokenRepository, passwordHasher, tokenProvider);
    }

    @Bean
    public LoginUseCase loginService(
            final UserRepository userRepository,
            final RefreshTokenRepository refreshTokenRepository,
            final PasswordHasher passwordHasher,
            final TokenProvider tokenProvider
    ) {
        return new LoginService(userRepository, refreshTokenRepository, passwordHasher, tokenProvider);
    }

    @Bean
    public RefreshUseCase refreshService(
            final RefreshTokenRepository refreshTokenRepository,
            final UserRepository userRepository,
            final TokenProvider tokenProvider
    ) {
        return new RefreshService(refreshTokenRepository, userRepository, tokenProvider);
    }

    @Bean
    public LogoutUseCase logoutService(final RefreshTokenRepository refreshTokenRepository) {
        return new LogoutService(refreshTokenRepository);
    }
}
