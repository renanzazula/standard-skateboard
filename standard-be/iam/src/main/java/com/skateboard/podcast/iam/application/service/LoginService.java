package com.skateboard.podcast.iam.application.service;

import com.skateboard.podcast.iam.application.dto.AuthResult;
import com.skateboard.podcast.iam.domain.port.out.PasswordHasher;
import com.skateboard.podcast.iam.domain.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.domain.port.out.TokenProvider;
import com.skateboard.podcast.iam.domain.port.out.UserRepository;
import com.skateboard.podcast.shared.domain.exception.UnauthorizedException;
import com.skateboard.podcast.shared.domain.exception.ValidationException;

import java.time.Instant;
import java.util.UUID;

public class LoginService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public LoginService(
            final UserRepository userRepository,
            final RefreshTokenRepository refreshTokenRepository,
            final PasswordHasher passwordHasher,
            final TokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    public AuthResult login(final String email, final String password, final String deviceId, final String deviceName) {
        if (email == null || email.isBlank()) throw new ValidationException("email cannot be blank");
        if (password == null || password.isBlank()) throw new ValidationException("password cannot be blank");
        if (deviceId == null || deviceId.isBlank()) throw new ValidationException("deviceId cannot be blank");

        final var user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("invalid credentials"));

        if (!"ACTIVE".equalsIgnoreCase(user.status())) {
            throw new UnauthorizedException("user not active");
        }
        if (!passwordHasher.matches(password, user.passwordHash())) {
            throw new UnauthorizedException("invalid credentials");
        }

        final String access = tokenProvider.createAccessToken(user.id(), user.role(), user.email());

        final String rawRefresh = tokenProvider.newRefreshToken();
        final String refreshHash = tokenProvider.hashRefreshToken(rawRefresh);

        final Instant now = Instant.now();
        final Instant refreshExp = now.plusSeconds(tokenProvider.refreshTtlSeconds());

        // v1 simple: new family per login (you can switch to "per device family" later)
        final UUID familyId = UUID.randomUUID();

        refreshTokenRepository.save(new RefreshTokenRepository.RefreshTokenRecord(
                UUID.randomUUID(),
                user.id(),
                refreshHash,
                familyId,
                deviceId,
                deviceName,
                now,
                refreshExp,
                null,
                null,
                null
        ));

        return new AuthResult(
                access,
                tokenProvider.accessTtlSeconds(),
                rawRefresh,
                tokenProvider.refreshTtlSeconds(),
                user.id(),
                user.email(),
                user.role(),
                user.provider()
        );
    }
}
