package com.skateboard.podcast.iam.service.application.service;

import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LogoutServiceTest {

    @Test
    void logoutRevokesUserTokens() {
        final InMemoryRefreshTokenRepository repo = new InMemoryRefreshTokenRepository();
        final LogoutService service = new LogoutService(repo);

        final UUID userId = UUID.randomUUID();
        service.logoutAllForUser(userId);

        assertEquals(userId, repo.lastUserId);
        assertNotNull(repo.lastRevokedAt);
    }

    private static final class InMemoryRefreshTokenRepository implements RefreshTokenRepository {
        private UUID lastUserId;
        private Instant lastRevokedAt;

        @Override
        public Optional<RefreshTokenRecord> findByTokenHash(final String tokenHash) {
            return Optional.empty();
        }

        @Override
        public RefreshTokenRecord save(final RefreshTokenRecord token) {
            return token;
        }

        @Override
        public void revokeById(final UUID id, final Instant revokedAt) {
            // not needed
        }

        @Override
        public void revokeByFamilyId(final UUID familyId, final Instant revokedAt) {
            // not needed
        }

        @Override
        public void revokeByUserId(final UUID userId, final Instant revokedAt) {
            this.lastUserId = userId;
            this.lastRevokedAt = revokedAt;
        }
    }
}
