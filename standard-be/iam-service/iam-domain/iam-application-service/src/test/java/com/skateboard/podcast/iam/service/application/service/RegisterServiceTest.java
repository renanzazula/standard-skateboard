package com.skateboard.podcast.iam.service.application.service;

import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserStatus;
import com.skateboard.podcast.iam.service.application.dto.AuthResult;
import com.skateboard.podcast.iam.service.application.port.out.PasswordHasher;
import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.service.application.port.out.TokenProvider;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisterServiceTest {

    @Test
    void registerRejectsInvalidPasswordOrDeviceId() {
        final RegisterService service = new RegisterService(
                new InMemoryUserRepository(),
                new InMemoryRefreshTokenRepository(),
                new FakePasswordHasher(),
                new FakeTokenProvider(900, 3600)
        );

        assertThrows(ValidationException.class, () -> service.register("user@example.com", "123", "device", "phone"));
        assertThrows(ValidationException.class, () -> service.register("user@example.com", "secret", " ", "phone"));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        final InMemoryUserRepository users = new InMemoryUserRepository();
        users.save(new UserRepository.UserRecord(
                UUID.randomUUID(),
                Email.of("user@example.com"),
                "hash:secret",
                Role.USER,
                Provider.MANUAL,
                UserStatus.ACTIVE
        ));

        final RegisterService service = new RegisterService(
                users,
                new InMemoryRefreshTokenRepository(),
                new FakePasswordHasher(),
                new FakeTokenProvider(900, 3600)
        );

        assertThrows(ValidationException.class, () -> service.register("user@example.com", "secret", "device", "phone"));
    }

    @Test
    void registerCreatesUserAndRefreshToken() {
        final InMemoryUserRepository users = new InMemoryUserRepository();
        final InMemoryRefreshTokenRepository tokens = new InMemoryRefreshTokenRepository();
        final FakeTokenProvider tokenProvider = new FakeTokenProvider(900, 3600);
        final RegisterService service = new RegisterService(
                users,
                tokens,
                new FakePasswordHasher(),
                tokenProvider
        );

        final AuthResult result = service.register("user@example.com", "secret", "device-1", "phone");

        final UserRepository.UserRecord storedUser = users.findByEmail(Email.of("user@example.com")).orElseThrow();
        assertEquals("hash:secret", storedUser.passwordHash());
        assertEquals(Role.USER, storedUser.role());
        assertEquals(Provider.MANUAL, storedUser.provider());

        assertEquals(storedUser.id(), result.userId().value());
        assertNotNull(result.accessToken());
        assertNotNull(result.refreshToken());

        final String tokenHash = tokenProvider.hashRefreshToken(result.refreshToken());
        final var storedToken = tokens.findByTokenHash(tokenHash).orElseThrow();
        assertEquals(storedUser.id(), storedToken.userId());
        assertEquals("device-1", storedToken.deviceId());
    }

    private static final class FakePasswordHasher implements PasswordHasher {
        @Override
        public String hash(final String rawPassword) {
            return "hash:" + rawPassword;
        }

        @Override
        public boolean matches(final String rawPassword, final String hash) {
            return hash(rawPassword).equals(hash);
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
            return "access-" + userId;
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
            if (existing == null) return;
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
                    .forEach(r -> revokeById(r.id(), revokedAt));
        }

        @Override
        public void revokeByUserId(final UUID userId, final Instant revokedAt) {
            byId.values().stream()
                    .filter(r -> userId.equals(r.userId()))
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
    }
}
