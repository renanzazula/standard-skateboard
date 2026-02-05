package com.skateboard.podcast.iam.service.application.service;

import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;

import java.time.Instant;
import java.util.UUID;

import com.skateboard.podcast.iam.service.application.port.in.LogoutUseCase;

public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutService(final RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void logoutAllForUser(final UUID userId) {
        refreshTokenRepository.revokeByUserId(userId, Instant.now());
    }
}
