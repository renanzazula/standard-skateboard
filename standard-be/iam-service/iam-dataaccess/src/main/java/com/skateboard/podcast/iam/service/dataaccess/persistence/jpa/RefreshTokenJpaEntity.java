package com.skateboard.podcast.iam.service.dataaccess.persistence.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "token_family_id", nullable = false)
    private UUID tokenFamilyId;

    @Column(name = "device_id", nullable = false, length = 128)
    private String deviceId;

    @Column(name = "device_name", length = 128)
    private String deviceName;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_id")
    private UUID replacedByTokenId;

    public UUID getId() { return id; }
    public void setId(final UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(final UUID userId) { this.userId = userId; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(final String tokenHash) { this.tokenHash = tokenHash; }

    public UUID getTokenFamilyId() { return tokenFamilyId; }
    public void setTokenFamilyId(final UUID tokenFamilyId) { this.tokenFamilyId = tokenFamilyId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(final String deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(final String deviceName) { this.deviceName = deviceName; }

    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(final Instant issuedAt) { this.issuedAt = issuedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(final Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(final Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(final Instant revokedAt) { this.revokedAt = revokedAt; }

    public UUID getReplacedByTokenId() { return replacedByTokenId; }
    public void setReplacedByTokenId(final UUID replacedByTokenId) { this.replacedByTokenId = replacedByTokenId; }
}
