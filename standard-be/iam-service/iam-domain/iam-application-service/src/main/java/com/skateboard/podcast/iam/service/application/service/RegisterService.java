package com.skateboard.podcast.iam.service.application.service;

import com.skateboard.podcast.iam.service.application.dto.AuthResult;
import com.skateboard.podcast.iam.service.application.port.out.PasswordHasher;
import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.service.application.port.out.TokenProvider;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserId;
import com.skateboard.podcast.domain.valueobject.UserStatus;

import java.time.Instant;
import java.util.UUID;

import com.skateboard.podcast.iam.service.application.port.in.RegisterUseCase;

public class RegisterService implements RegisterUseCase {

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

    @Override
    public AuthResult register(final String email, final String password, final String deviceId, final String deviceName) {
        if (password == null || password.length() < 6) throw new ValidationException("password must be at least 6 chars");
        if (deviceId == null || deviceId.isBlank()) throw new ValidationException("deviceId cannot be blank");

        final Email emailValue = Email.of(email);

        userRepository.findByEmail(emailValue).ifPresent(u -> {
            throw new ValidationException("email already registered");
        });

        final UUID userId = UUID.randomUUID();
        final String passwordHash = passwordHasher.hash(password);

        final var user = new UserRepository.UserRecord(
                userId,
                emailValue,
                passwordHash,
                Role.USER,
                Provider.MANUAL,
                UserStatus.ACTIVE
        );

        userRepository.save(user);

        final String access = tokenProvider.createAccessToken(
                userId,
                user.role().name(),
                user.email().value()
        );
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
                UserId.of(userId),
                user.email(),
                user.role(),
                user.provider()
        );
    }
}
