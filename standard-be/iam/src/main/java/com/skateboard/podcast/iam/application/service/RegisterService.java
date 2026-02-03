package com.skateboard.podcast.iam.application.service;

import com.skateboard.podcast.iam.application.dto.AuthResult;
import com.skateboard.podcast.iam.domain.port.out.PasswordHasher;
import com.skateboard.podcast.iam.domain.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.domain.port.out.TokenProvider;
import com.skateboard.podcast.iam.domain.port.out.UserRepository;
import com.skateboard.podcast.shared.domain.exception.ValidationException;

import java.time.Instant;
import java.util.UUID;

public class RegisterService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public RegisterService(
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

    public AuthResult register(final String email, final String password, final String deviceId, final String deviceName) {
        if (email == null || email.isBlank()) throw new ValidationException("email cannot be blank");
        if (password == null || password.length() < 6) throw new ValidationException("password must be at least 6 chars");
        if (deviceId == null || deviceId.isBlank()) throw new ValidationException("deviceId cannot be blank");

        userRepository.findByEmail(email.trim().toLowerCase()).ifPresent(u -> {
            throw new ValidationException("email already registered");
        });

        final UUID userId = UUID.randomUUID();
        final String passwordHash = passwordHasher.hash(password);

        final var user = new UserRepository.UserRecord(
                userId,
                email.trim().toLowerCase(),
                passwordHash,
                "USER",
                "MANUAL",
                "ACTIVE"
        );

        userRepository.save(user);

        final String access = tokenProvider.createAccessToken(userId, user.role(), user.email());
        final String rawRefresh = tokenProvider.newRefreshToken();
        final String refreshHash = tokenProvider.hashRefreshToken(rawRefresh);

        final Instant now = Instant.now();
        final Instant refreshExp = now.plusSeconds(tokenProvider.refreshTtlSeconds());
        final UUID familyId = UUID.randomUUID();

        refreshTokenRepository.save(new RefreshTokenRepository.RefreshTokenRecord(
                UUID.randomUUID(),
                userId,
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
                userId,
                user.email(),
                user.role(),
                user.provider()
        );
    }
}
