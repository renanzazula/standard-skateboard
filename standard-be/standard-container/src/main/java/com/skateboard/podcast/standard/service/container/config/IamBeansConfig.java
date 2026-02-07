package com.skateboard.podcast.standard.service.container.config;

import com.skateboard.podcast.iam.service.application.service.AdminPasscodeLoginService;
import com.skateboard.podcast.iam.service.application.service.LoginService;
import com.skateboard.podcast.iam.service.application.service.LogoutService;
import com.skateboard.podcast.iam.service.application.service.RefreshService;
import com.skateboard.podcast.iam.service.application.service.RegisterService;
import com.skateboard.podcast.iam.service.application.service.SocialLoginService;
import com.skateboard.podcast.iam.service.application.port.in.AdminPasscodeLoginUseCase;
import com.skateboard.podcast.iam.service.application.port.in.LoginUseCase;
import com.skateboard.podcast.iam.service.application.port.in.LogoutUseCase;
import com.skateboard.podcast.iam.service.application.port.in.RefreshUseCase;
import com.skateboard.podcast.iam.service.application.port.in.RegisterUseCase;
import com.skateboard.podcast.iam.service.application.port.in.SocialLoginUseCase;
import com.skateboard.podcast.iam.service.application.port.out.PasswordHasher;
import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.service.application.port.out.TokenProvider;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository;
import org.springframework.beans.factory.annotation.Value;
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

    @Bean
    public AdminPasscodeLoginUseCase adminPasscodeLoginService(
            final UserRepository userRepository,
            final RefreshTokenRepository refreshTokenRepository,
            final TokenProvider tokenProvider,
            @Value("${security.admin.passcode:}") final String passcode,
            @Value("${security.admin.email:admin@example.com}") final String adminEmail
    ) {
        return new AdminPasscodeLoginService(
                userRepository,
                refreshTokenRepository,
                tokenProvider,
                passcode,
                adminEmail
        );
    }

    @Bean
    public SocialLoginUseCase socialLoginService(
            final UserRepository userRepository,
            final RefreshTokenRepository refreshTokenRepository,
            final TokenProvider tokenProvider
    ) {
        return new SocialLoginService(userRepository, refreshTokenRepository, tokenProvider);
    }
}
