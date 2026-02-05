package com.skateboard.podcast.iam.service.dataaccess.persistence.jpa;


import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 16)
    private String role;

    @Column(name = "provider", nullable = false, length = 16)
    private String provider;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(final UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(final String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(final String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(final String role) { this.role = role; }

    public String getProvider() { return provider; }
    public void setProvider(final String provider) { this.provider = provider; }

    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(final Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(final Instant updatedAt) { this.updatedAt = updatedAt; }
}
