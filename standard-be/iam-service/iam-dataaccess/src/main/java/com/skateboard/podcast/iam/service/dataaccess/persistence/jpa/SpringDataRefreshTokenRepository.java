package com.skateboard.podcast.iam.service.dataaccess.persistence.jpa;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshTokenJpaEntity t
            set t.revokedAt = :revokedAt
            where t.tokenFamilyId = :familyId and t.revokedAt is null
            """)
    int revokeByFamilyId(@Param("familyId") UUID familyId, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("""
            update RefreshTokenJpaEntity t
            set t.revokedAt = :revokedAt
            where t.userId = :userId and t.revokedAt is null
            """)
    int revokeByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);
}
