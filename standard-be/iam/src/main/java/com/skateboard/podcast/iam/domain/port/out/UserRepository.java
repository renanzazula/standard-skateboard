package com.skateboard.podcast.iam.domain.port.out;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<UserRecord> findByEmail(String email);

    UserRecord save(UserRecord user);

    record UserRecord(
            UUID id,
            String email,
            String passwordHash,
            String role,
            String provider,
            String status
    ) {}
}
