package com.skateboard.podcast.iam.service.dataaccess.persistence;

import com.skateboard.podcast.iam.service.application.port.out.UserRepository;
import com.skateboard.podcast.iam.service.dataaccess.persistence.jpa.SpringDataUserRepository;
import com.skateboard.podcast.iam.service.dataaccess.persistence.jpa.UserJpaEntity;
import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserStatus;
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
    public Optional<UserRecord> findByEmail(final Email email) {
        return repo.findByEmail(email.value()).map(UserRepositoryAdapter::toRecord);
    }

    @Override
    public Optional<UserRecord> findById(final java.util.UUID id) {
        return repo.findById(id).map(UserRepositoryAdapter::toRecord);
    }

    @Override
    public UserRecord save(final UserRecord user) {
        final UserJpaEntity e = new UserJpaEntity();
        e.setId(user.id());
        e.setEmail(user.email().value());
        e.setPasswordHash(user.passwordHash());
        e.setRole(user.role().name());
        e.setProvider(user.provider().name());
        e.setStatus(user.status().name());
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        repo.save(e);
        return user;
    }

    private static UserRecord toRecord(final UserJpaEntity e) {
        return new UserRecord(
                e.getId(),
                Email.of(e.getEmail()),
                e.getPasswordHash(),
                Role.from(e.getRole()),
                Provider.from(e.getProvider()),
                UserStatus.from(e.getStatus())
        );
    }
}
