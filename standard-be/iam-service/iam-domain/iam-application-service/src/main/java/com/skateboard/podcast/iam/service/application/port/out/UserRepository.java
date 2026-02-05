package com.skateboard.podcast.iam.service.application.port.out;

import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserStatus;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<UserRecord> findByEmail(Email email);

    Optional<UserRecord> findById(UUID id);

    UserRecord save(UserRecord user);

    record UserRecord(
            UUID id,
            Email email,
            String passwordHash,
            Role role,
            Provider provider,
            UserStatus status
    ) {}
}
