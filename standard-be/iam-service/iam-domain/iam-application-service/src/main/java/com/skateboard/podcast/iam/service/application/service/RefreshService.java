package com.skateboard.podcast.iam.service.application.service;

import com.skateboard.podcast.iam.service.application.dto.AuthResult;
import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.service.application.port.out.TokenProvider;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository;
import com.skateboard.podcast.domain.exception.UnauthorizedException;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.UserId;
import com.skateboard.podcast.domain.valueobject.UserStatus;

import java.time.Instant;
import java.util.UUID;

import com.skateboard.podcast.iam.service.application.port.in.RefreshUseCase;

public class RefreshService implements RefreshUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public RefreshService(
            final RefreshTokenRepository refreshTokenRepository,
            final UserRepository userRepository,
            final TokenProvider tokenProvider
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResult refresh(final String rawRefreshToken, final String deviceId) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new ValidationException("refreshToken cannot be blank");
        }
        if (deviceId == null || deviceId.isBlank()) {
            throw new ValidationException("deviceId cannot be blank");
        }

        final String tokenHash = tokenProvider.hashRefreshToken(rawRefreshToken);
        final var token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("invalid refresh token"));

        final Instant now = Instant.now();
        if (token.revokedAt() != null || token.replacedByTokenId() != null) {
            refreshTokenRepository.revokeByFamilyId(token.tokenFamilyId(), now);
            throw new UnauthorizedException("refresh token revoked");
        }
        if (now.isAfter(token.expiresAt())) {
            throw new UnauthorizedException("refresh token expired");
        }
        if (!deviceId.equals(token.deviceId())) {
            refreshTokenRepository.revokeByFamilyId(token.tokenFamilyId(), now);
            throw new UnauthorizedException("refresh token device mismatch");
        }

        final var user = userRepository.findById(token.userId())
                .orElseThrow(() -> new UnauthorizedException("user not found"));
        if (user.status() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("user not active");
        }

        final String access = tokenProvider.createAccessToken(
                user.id(),
                user.role().name(),
                user.email().value()
        );
        final String rawRefresh = tokenProvider.newRefreshToken();
        final String refreshHash = tokenProvider.hashRefreshToken(rawRefresh);
        final Instant refreshExp = now.plusSeconds(tokenProvider.refreshTtlSeconds());

        final UUID newTokenId = UUID.randomUUID();
        final var newToken = new RefreshTokenRepository.RefreshTokenRecord(
                newTokenId,
                token.userId(),
                refreshHash,
                token.tokenFamilyId(),
                token.deviceId(),
                token.deviceName(),
                now,
                refreshExp,
                null,
                null,
                null
        );
        refreshTokenRepository.save(newToken);

        final var oldTokenUpdated = new RefreshTokenRepository.RefreshTokenRecord(
                token.id(),
                token.userId(),
                token.tokenHash(),
                token.tokenFamilyId(),
                token.deviceId(),
                token.deviceName(),
                token.issuedAt(),
                token.expiresAt(),
                now,
                now,
                newTokenId
        );
        refreshTokenRepository.save(oldTokenUpdated);

        return new AuthResult(
                access,
                tokenProvider.accessTtlSeconds(),
                rawRefresh,
                tokenProvider.refreshTtlSeconds(),
                UserId.of(user.id()),
                user.email(),
                user.role(),
                user.provider(),
                user.name(),
                user.avatarUrl()
        );
    }
}
