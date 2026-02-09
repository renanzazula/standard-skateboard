package com.skateboard.podcast.iam.service.application.service;

import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.UserStatus;
import com.skateboard.podcast.iam.service.application.dto.AdminUserUpdateCommand;
import com.skateboard.podcast.iam.service.application.dto.AdminUserView;
import com.skateboard.podcast.iam.service.application.port.in.AdminUsersUseCase;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AdminUsersService implements AdminUsersUseCase {

    private final UserRepository userRepository;

    public AdminUsersService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<AdminUserView> list() {
        return userRepository.findAll().stream()
                .map(AdminUsersService::toView)
                .toList();
    }

    @Override
    public AdminUserView update(
            final UUID userId,
            final AdminUserUpdateCommand command,
            final UUID actorUserId
    ) {
        if (command == null) {
            throw new ValidationException("update payload cannot be null");
        }

        final var user = requireUser(userId);

        final String name = normalizeOptional(command.name(), "name");
        final String username = normalizeOptional(command.username(), "username");

        if (actorUserId != null && actorUserId.equals(userId)) {
            if (command.role() != null && command.role() != user.role()) {
                throw new ValidationException("cannot change your own role");
            }
        }

        if (name == null && username == null && command.role() == null) {
            throw new ValidationException("update payload is empty");
        }

        final Instant now = Instant.now();
        final var updated = new UserRepository.UserRecord(
                user.id(),
                user.email(),
                user.passwordHash(),
                command.role() == null ? user.role() : command.role(),
                user.provider(),
                user.status(),
                name == null ? user.name() : name,
                username == null ? user.username() : username,
                user.avatarUrl(),
                user.createdAt(),
                now,
                user.lastLoginAt()
        );
        userRepository.save(updated);
        return toView(updated);
    }

    @Override
    public AdminUserView updateStatus(final UUID userId, final UserStatus status, final UUID actorUserId) {
        if (status == null) {
            throw new ValidationException("status cannot be null");
        }
        if (actorUserId != null && actorUserId.equals(userId)) {
            throw new ValidationException("cannot change your own status");
        }

        final var user = requireUser(userId);
        final Instant now = Instant.now();
        final var updated = new UserRepository.UserRecord(
                user.id(),
                user.email(),
                user.passwordHash(),
                user.role(),
                user.provider(),
                status,
                user.name(),
                user.username(),
                user.avatarUrl(),
                user.createdAt(),
                now,
                user.lastLoginAt()
        );
        userRepository.save(updated);
        return toView(updated);
    }

    @Override
    public void delete(final UUID userId, final UUID actorUserId) {
        if (actorUserId != null && actorUserId.equals(userId)) {
            throw new ValidationException("cannot delete your own account");
        }
        requireUser(userId);
        userRepository.deleteById(userId);
    }

    private UserRepository.UserRecord requireUser(final UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("user not found"));
    }

    private static String normalizeOptional(final String value, final String fieldName) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException(fieldName + " cannot be blank");
        }
        return trimmed;
    }

    private static AdminUserView toView(final UserRepository.UserRecord record) {
        return new AdminUserView(
                record.id(),
                record.email(),
                record.role(),
                record.provider(),
                record.status(),
                record.name(),
                record.username(),
                record.avatarUrl(),
                record.createdAt(),
                record.updatedAt(),
                record.lastLoginAt()
        );
    }
}
