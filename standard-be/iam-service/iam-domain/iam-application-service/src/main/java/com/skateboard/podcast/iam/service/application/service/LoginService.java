package com.skateboard.podcast.iam.service.application.service;

import com.skateboard.podcast.iam.service.application.dto.AuthResult;
import com.skateboard.podcast.iam.service.application.port.out.PasswordHasher;
import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.service.application.port.out.TokenProvider;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository;
import com.skateboard.podcast.domain.exception.UnauthorizedException;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.UserId;
import com.skateboard.podcast.domain.valueobject.UserStatus;

import java.time.Instant;
import java.util.UUID;

import com.skateboard.podcast.iam.service.application.port.in.LoginUseCase;

public class LoginService implements LoginUseCase {

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

    @Override
    public AuthResult login(final String email, final String password, final String deviceId, final String deviceName) {
        if (password == null || password.isBlank()) throw new ValidationException("password cannot be blank");
        if (deviceId == null || deviceId.isBlank()) throw new ValidationException("deviceId cannot be blank");

        final Email emailValue = Email.of(email);

        final var user = userRepository.findByEmail(emailValue)
                .orElseThrow(() -> new UnauthorizedException("invalid credentials"));

        if (user.status() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("user not active");
        }
        if (!passwordHasher.matches(password, user.passwordHash())) {
            throw new UnauthorizedException("invalid credentials");
        }

        final Instant now = Instant.now();
        final var updatedUser = new UserRepository.UserRecord(
                user.id(),
                user.email(),
                user.passwordHash(),
                user.role(),
                user.provider(),
                user.status(),
                user.name(),
                user.username(),
                user.avatarUrl(),
                user.createdAt(),
                now,
                now
        );
        userRepository.save(updatedUser);

        final String access = tokenProvider.createAccessToken(
                updatedUser.id(),
                updatedUser.role().name(),
                updatedUser.email().value()
        );

        final String rawRefresh = tokenProvider.newRefreshToken();
        final String refreshHash = tokenProvider.hashRefreshToken(rawRefresh);

        final Instant refreshExp = now.plusSeconds(tokenProvider.refreshTtlSeconds());

        // v1 simple: new family per login (you can switch to "per device family" later)
        final UUID familyId = UUID.randomUUID();

        refreshTokenRepository.save(new RefreshTokenRepository.RefreshTokenRecord(
                UUID.randomUUID(),
                updatedUser.id(),
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
                UserId.of(updatedUser.id()),
                updatedUser.email(),
                updatedUser.role(),
                updatedUser.provider(),
                updatedUser.name(),
                updatedUser.avatarUrl()
        );
    }
}
