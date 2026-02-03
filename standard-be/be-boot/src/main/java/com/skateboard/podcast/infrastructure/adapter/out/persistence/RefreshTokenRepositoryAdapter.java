package com.skateboard.podcast.infrastructure.adapter.out.persistence;

import com.skateboard.podcast.iam.domain.port.out.RefreshTokenRepository;
import com.skateboard.podcast.infrastructure.adapter.out.persistence.jpa.RefreshTokenJpaEntity;
import com.skateboard.podcast.infrastructure.adapter.out.persistence.jpa.SpringDataRefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository repo;

    public RefreshTokenRepositoryAdapter(final SpringDataRefreshTokenRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<RefreshTokenRecord> findByTokenHash(final String tokenHash) {
        return repo.findByTokenHash(tokenHash).map(RefreshTokenRepositoryAdapter::toRecord);
    }

    @Override
    public RefreshTokenRecord save(final RefreshTokenRecord token) {
        final RefreshTokenJpaEntity e = new RefreshTokenJpaEntity();
        e.setId(token.id());
        e.setUserId(token.userId());
        e.setTokenHash(token.tokenHash());
        e.setTokenFamilyId(token.tokenFamilyId());
        e.setDeviceId(token.deviceId());
        e.setDeviceName(token.deviceName());
        e.setIssuedAt(token.issuedAt());
        e.setExpiresAt(token.expiresAt());
        e.setLastUsedAt(token.lastUsedAt());
        e.setRevokedAt(token.revokedAt());
        e.setReplacedByTokenId(token.replacedByTokenId());
        repo.save(e);
        return token;
    }

    @Override
    public void revokeById(final UUID id, final Instant revokedAt) {
        repo.findById(id).ifPresent(e -> {
            e.setRevokedAt(revokedAt);
            repo.save(e);
        });
    }

    private static RefreshTokenRecord toRecord(final RefreshTokenJpaEntity e) {
        return new RefreshTokenRecord(
                e.getId(),
                e.getUserId(),
                e.getTokenHash(),
                e.getTokenFamilyId(),
                e.getDeviceId(),
                e.getDeviceName(),
                e.getIssuedAt(),
                e.getExpiresAt(),
                e.getLastUsedAt(),
                e.getRevokedAt(),
                e.getReplacedByTokenId()
        );
    }
}
