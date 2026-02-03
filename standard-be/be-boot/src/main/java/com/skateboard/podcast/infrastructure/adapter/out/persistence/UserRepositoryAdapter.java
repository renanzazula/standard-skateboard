package com.skateboard.podcast.infrastructure.adapter.out.persistence;

import com.skateboard.podcast.iam.domain.port.out.UserRepository;
import com.skateboard.podcast.infrastructure.adapter.out.persistence.jpa.SpringDataUserRepository;
import com.skateboard.podcast.infrastructure.adapter.out.persistence.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository repo;

    public UserRepositoryAdapter(final SpringDataUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<UserRecord> findByEmail(final String email) {
        return repo.findByEmail(email).map(UserRepositoryAdapter::toRecord);
    }

    @Override
    public UserRecord save(final UserRecord user) {
        final UserJpaEntity e = new UserJpaEntity();
        e.setId(user.id());
        e.setEmail(user.email());
        e.setPasswordHash(user.passwordHash());
        e.setRole(user.role());
        e.setProvider(user.provider());
        e.setStatus(user.status());
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        repo.save(e);
        return user;
    }

    private static UserRecord toRecord(final UserJpaEntity e) {
        return new UserRecord(
                e.getId(),
                e.getEmail(),
                e.getPasswordHash(),
                e.getRole(),
                e.getProvider(),
                e.getStatus()
        );
    }
}
