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
import java.util.List;
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
    public List<UserRecord> findAll() {
        return repo.findAll().stream()
                .map(UserRepositoryAdapter::toRecord)
                .toList();
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
        e.setName(user.name());
        e.setUsername(user.username());
        e.setAvatarUrl(user.avatarUrl());
        e.setCreatedAt(user.createdAt() == null ? Instant.now() : user.createdAt());
        e.setUpdatedAt(user.updatedAt() == null ? Instant.now() : user.updatedAt());
        e.setLastLoginAt(user.lastLoginAt());
        repo.save(e);
        return user;
    }

    @Override
    public void deleteById(final java.util.UUID id) {
        repo.deleteById(id);
    }

    private static UserRecord toRecord(final UserJpaEntity e) {
        return new UserRecord(
                e.getId(),
                Email.of(e.getEmail()),
                e.getPasswordHash(),
                Role.from(e.getRole()),
                Provider.from(e.getProvider()),
                UserStatus.from(e.getStatus()),
                e.getName(),
                e.getUsername(),
                e.getAvatarUrl(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getLastLoginAt()
        );
    }
}
