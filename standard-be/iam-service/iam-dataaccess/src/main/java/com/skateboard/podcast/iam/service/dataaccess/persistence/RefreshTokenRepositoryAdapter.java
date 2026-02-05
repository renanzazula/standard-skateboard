package com.skateboard.podcast.iam.service.dataaccess.persistence;

import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.service.dataaccess.persistence.jpa.RefreshTokenJpaEntity;
import com.skateboard.podcast.iam.service.dataaccess.persistence.jpa.SpringDataRefreshTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void revokeById(final UUID id, final Instant revokedAt) {
        repo.findById(id).ifPresent(e -> {
            e.setRevokedAt(revokedAt);
            repo.save(e);
        });
    }

    @Override
    @Transactional
    public void revokeByFamilyId(final UUID familyId, final Instant revokedAt) {
        repo.revokeByFamilyId(familyId, revokedAt);
    }

    @Override
    @Transactional
    public void revokeByUserId(final UUID userId, final Instant revokedAt) {
        repo.revokeByUserId(userId, revokedAt);
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
