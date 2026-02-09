package com.skateboard.podcast.iam.service.application.service;

import com.skateboard.podcast.iam.service.application.dto.AuthResult;
import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.service.application.port.out.TokenProvider;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository;
import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserStatus;
import com.skateboard.podcast.domain.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshServiceTest {

    @Test
    void refreshRotatesTokens() {
        final TokenProvider tokenProvider = new FakeTokenProvider(900, 3600);

        final InMemoryRefreshTokenRepository refreshRepo = new InMemoryRefreshTokenRepository();
        final InMemoryUserRepository userRepo = new InMemoryUserRepository();
        final RefreshService service = new RefreshService(refreshRepo, userRepo, tokenProvider);

        final UUID userId = UUID.randomUUID();
        final Instant now = Instant.now();
        userRepo.save(new UserRepository.UserRecord(
                userId,
                Email.of("user@example.com"),
                "hash",
                Role.USER,
                Provider.MANUAL,
                UserStatus.ACTIVE,
                "Skater",
                "skater",
                null,
                now,
                now,
                null
        ));

        final UUID familyId = UUID.randomUUID();
        final String rawRefresh = tokenProvider.newRefreshToken();
        final String hash = tokenProvider.hashRefreshToken(rawRefresh);
        final Instant issuedAt = Instant.now().minusSeconds(5);
        final var record = new RefreshTokenRepository.RefreshTokenRecord(
                UUID.randomUUID(),
                userId,
                hash,
                familyId,
                "device-1",
                "phone",
                issuedAt,
                issuedAt.plusSeconds(tokenProvider.refreshTtlSeconds()),
                null,
                null,
                null
        );
        refreshRepo.save(record);

        final AuthResult result = service.refresh(rawRefresh, "device-1");

        assertNotEquals(rawRefresh, result.refreshToken());

        final var oldToken = refreshRepo.findByTokenHash(hash).orElseThrow();
        assertNotNull(oldToken.revokedAt());
        assertNotNull(oldToken.replacedByTokenId());
        assertNotNull(oldToken.lastUsedAt());

        final String newHash = tokenProvider.hashRefreshToken(result.refreshToken());
        final var newToken = refreshRepo.findByTokenHash(newHash).orElseThrow();
        assertEquals(familyId, newToken.tokenFamilyId());
        assertNull(newToken.revokedAt());
    }

    @Test
    void revokedTokenRevokesFamily() {
        final TokenProvider tokenProvider = new FakeTokenProvider(900, 3600);

        final InMemoryRefreshTokenRepository refreshRepo = new InMemoryRefreshTokenRepository();
        final InMemoryUserRepository userRepo = new InMemoryUserRepository();
        final RefreshService service = new RefreshService(refreshRepo, userRepo, tokenProvider);

        final UUID userId = UUID.randomUUID();
        final Instant now = Instant.now();
        userRepo.save(new UserRepository.UserRecord(
                userId,
                Email.of("user@example.com"),
                "hash",
                Role.USER,
                Provider.MANUAL,
                UserStatus.ACTIVE,
                "Skater",
                "skater",
                null,
                now,
                now,
                null
        ));

        final UUID familyId = UUID.randomUUID();
        final String rawA = tokenProvider.newRefreshToken();
        final String hashA = tokenProvider.hashRefreshToken(rawA);
        final Instant issuedAt = Instant.now().minusSeconds(5);
        refreshRepo.save(new RefreshTokenRepository.RefreshTokenRecord(
                UUID.randomUUID(),
                userId,
                hashA,
                familyId,
                "device-1",
                "phone",
                issuedAt,
                issuedAt.plusSeconds(tokenProvider.refreshTtlSeconds()),
                null,
                Instant.now().minusSeconds(1),
                null
        ));

        final String rawB = tokenProvider.newRefreshToken();
        final String hashB = tokenProvider.hashRefreshToken(rawB);
        refreshRepo.save(new RefreshTokenRepository.RefreshTokenRecord(
                UUID.randomUUID(),
                userId,
                hashB,
                familyId,
                "device-1",
                "phone",
                issuedAt,
                issuedAt.plusSeconds(tokenProvider.refreshTtlSeconds()),
                null,
                null,
                null
        ));

        assertThrows(UnauthorizedException.class, () -> service.refresh(rawA, "device-1"));

        final var tokenB = refreshRepo.findByTokenHash(hashB).orElseThrow();
        assertNotNull(tokenB.revokedAt());
    }

    private static final class InMemoryRefreshTokenRepository implements RefreshTokenRepository {
        private final Map<UUID, RefreshTokenRecord> byId = new HashMap<>();
        private final Map<String, UUID> byHash = new HashMap<>();

        @Override
        public Optional<RefreshTokenRecord> findByTokenHash(final String tokenHash) {
            final UUID id = byHash.get(tokenHash);
            return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
        }

        @Override
        public RefreshTokenRecord save(final RefreshTokenRecord token) {
            byId.put(token.id(), token);
            byHash.put(token.tokenHash(), token.id());
            return token;
        }

        @Override
        public void revokeById(final UUID id, final Instant revokedAt) {
            final RefreshTokenRecord existing = byId.get(id);
            if (existing == null || existing.revokedAt() != null) return;
            save(new RefreshTokenRecord(
                    existing.id(),
                    existing.userId(),
                    existing.tokenHash(),
                    existing.tokenFamilyId(),
                    existing.deviceId(),
                    existing.deviceName(),
                    existing.issuedAt(),
                    existing.expiresAt(),
                    existing.lastUsedAt(),
                    revokedAt,
                    existing.replacedByTokenId()
            ));
        }

        @Override
        public void revokeByFamilyId(final UUID familyId, final Instant revokedAt) {
            byId.values().stream()
                    .filter(r -> familyId.equals(r.tokenFamilyId()))
                    .filter(r -> r.revokedAt() == null)
                    .forEach(r -> revokeById(r.id(), revokedAt));
        }

        @Override
        public void revokeByUserId(final UUID userId, final Instant revokedAt) {
            byId.values().stream()
                    .filter(r -> userId.equals(r.userId()))
                    .filter(r -> r.revokedAt() == null)
                    .forEach(r -> revokeById(r.id(), revokedAt));
        }
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<UUID, UserRecord> byId = new HashMap<>();
        private final Map<String, UUID> byEmail = new HashMap<>();

        @Override
        public Optional<UserRecord> findByEmail(final Email email) {
            final UUID id = byEmail.get(email.value());
            return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<UserRecord> findById(final UUID id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public UserRecord save(final UserRecord user) {
            byId.put(user.id(), user);
            byEmail.put(user.email().value(), user.id());
            return user;
        }

        @Override
        public java.util.List<UserRecord> findAll() {
            return byId.values().stream().toList();
        }

        @Override
        public void deleteById(final UUID id) {
            final UserRecord removed = byId.remove(id);
            if (removed != null) {
                byEmail.remove(removed.email().value());
            }
        }
    }

    private static final class FakeTokenProvider implements TokenProvider {
        private final long accessTtlSeconds;
        private final long refreshTtlSeconds;

        private FakeTokenProvider(final long accessTtlSeconds, final long refreshTtlSeconds) {
            this.accessTtlSeconds = accessTtlSeconds;
            this.refreshTtlSeconds = refreshTtlSeconds;
        }

        @Override
        public String createAccessToken(final UUID userId, final String role, final String email) {
            return "access-token";
        }

        @Override
        public String newRefreshToken() {
            return "refresh-" + UUID.randomUUID();
        }

        @Override
        public String hashRefreshToken(final String rawRefreshToken) {
            return "hash-" + rawRefreshToken;
        }

        @Override
        public long accessTtlSeconds() {
            return accessTtlSeconds;
        }

        @Override
        public long refreshTtlSeconds() {
            return refreshTtlSeconds;
        }
    }
}
