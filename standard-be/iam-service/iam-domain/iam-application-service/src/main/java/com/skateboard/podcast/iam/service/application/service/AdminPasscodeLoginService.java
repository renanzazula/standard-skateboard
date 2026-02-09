package com.skateboard.podcast.iam.service.application.service;

import com.skateboard.podcast.domain.exception.UnauthorizedException;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserId;
import com.skateboard.podcast.domain.valueobject.UserStatus;
import com.skateboard.podcast.iam.service.application.dto.AuthResult;
import com.skateboard.podcast.iam.service.application.port.in.AdminPasscodeLoginUseCase;
import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.service.application.port.out.TokenProvider;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository;

import java.time.Instant;
import java.util.UUID;

public class AdminPasscodeLoginService implements AdminPasscodeLoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final String passcode;
    private final Email adminEmail;

    public AdminPasscodeLoginService(
            final UserRepository userRepository,
            final RefreshTokenRepository refreshTokenRepository,
            final TokenProvider tokenProvider,
            final String passcode,
            final String adminEmail
    ) {
        if (passcode == null || passcode.isBlank()) {
            throw new ValidationException("admin passcode is not configured");
        }
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
        this.passcode = passcode.trim();
        this.adminEmail = Email.of(adminEmail == null ? "admin@example.com" : adminEmail);
    }

    @Override
    public AuthResult login(final String passcode, final String deviceId, final String deviceName) {
        if (passcode == null || passcode.isBlank()) {
            throw new ValidationException("passcode cannot be blank");
        }
        if (deviceId == null || deviceId.isBlank()) {
            throw new ValidationException("deviceId cannot be blank");
        }
        if (!this.passcode.equals(passcode.trim())) {
            throw new UnauthorizedException("invalid passcode");
        }

        final var user = userRepository.findByEmail(adminEmail)
                .orElseGet(() -> createAdminUser(adminEmail));

        if (user.status() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("user not active");
        }
        if (user.role() != Role.ADMIN) {
            throw new UnauthorizedException("user not allowed");
        }

        return issueTokens(user, deviceId, deviceName);
    }

    private UserRepository.UserRecord createAdminUser(final Email email) {
        final Instant now = Instant.now();
        final var user = new UserRepository.UserRecord(
                UUID.randomUUID(),
                email,
                null,
                Role.ADMIN,
                Provider.PASSCODE,
                UserStatus.ACTIVE,
                "Admin",
                "admin",
                null,
                now,
                now,
                null
        );
        return userRepository.save(user);
    }

    private AuthResult issueTokens(
            final UserRepository.UserRecord user,
            final String deviceId,
            final String deviceName
    ) {
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
