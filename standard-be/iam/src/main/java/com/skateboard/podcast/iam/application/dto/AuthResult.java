package com.skateboard.podcast.iam.application.dto;

import java.util.UUID;

public record AuthResult(
        String accessToken,
        long expiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds,
        UUID userId,
        String email,
        String role,
        String provider
) {}
