package com.skateboard.podcast.iam.domain.port.out;

import java.util.UUID;

public interface TokenProvider {
    String createAccessToken(UUID userId, String role, String email);
    String newRefreshToken();
    String hashRefreshToken(String rawRefreshToken);

    long accessTtlSeconds();
    long refreshTtlSeconds();
}
