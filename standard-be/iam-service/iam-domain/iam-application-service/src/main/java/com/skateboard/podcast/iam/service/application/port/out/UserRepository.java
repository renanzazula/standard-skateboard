package com.skateboard.podcast.iam.service.application.port.out;

import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<UserRecord> findByEmail(Email email);

    Optional<UserRecord> findById(UUID id);

    List<UserRecord> findAll();

    UserRecord save(UserRecord user);

    void deleteById(UUID id);

    record UserRecord(
            UUID id,
            Email email,
            String passwordHash,
            Role role,
            Provider provider,
            UserStatus status,
            String name,
            String username,
            String avatarUrl,
            Instant createdAt,
            Instant updatedAt,
            Instant lastLoginAt
    ) {}
}
