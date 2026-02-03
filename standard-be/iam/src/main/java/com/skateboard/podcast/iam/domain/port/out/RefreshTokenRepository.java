package com.skateboard.podcast.iam.domain.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    Optional<RefreshTokenRecord> findByTokenHash(String tokenHash);

    RefreshTokenRecord save(RefreshTokenRecord token);

    void revokeById(UUID id, Instant revokedAt);

    record RefreshTokenRecord(
            UUID id,
            UUID userId,
            String tokenHash,
            UUID tokenFamilyId,
            String deviceId,
            String deviceName,
            Instant issuedAt,
            Instant expiresAt,
            Instant lastUsedAt,
            Instant revokedAt,
            UUID replacedByTokenId
    ) {}
}
