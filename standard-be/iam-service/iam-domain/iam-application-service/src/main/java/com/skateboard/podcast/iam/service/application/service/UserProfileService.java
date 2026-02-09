package com.skateboard.podcast.iam.service.application.service;

import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.iam.service.application.dto.UserProfileUpdateCommand;
import com.skateboard.podcast.iam.service.application.dto.UserProfileView;
import com.skateboard.podcast.iam.service.application.port.in.UserProfileUseCase;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository;

import java.time.Instant;
import java.util.UUID;

public class UserProfileService implements UserProfileUseCase {

    private final UserRepository userRepository;

    public UserProfileService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserProfileView get(final UUID userId) {
        return toView(requireUser(userId));
    }

    @Override
    public UserProfileView update(final UUID userId, final UserProfileUpdateCommand command) {
        if (command == null) {
            throw new ValidationException("update payload cannot be null");
        }

        final var user = requireUser(userId);

        final String name = normalizeOptional(command.name(), "name");
        final String username = normalizeOptional(command.username(), "username");
        final String avatarUrl = normalizeOptional(command.avatarUrl(), "avatarUrl");

        if (name == null && username == null && avatarUrl == null) {
            throw new ValidationException("update payload is empty");
        }

        final Instant now = Instant.now();
        final var updated = new UserRepository.UserRecord(
                user.id(),
                user.email(),
                user.passwordHash(),
                user.role(),
                user.provider(),
                user.status(),
                name == null ? user.name() : name,
                username == null ? user.username() : username,
                avatarUrl == null ? user.avatarUrl() : avatarUrl,
                user.createdAt(),
                now,
                user.lastLoginAt()
        );
        userRepository.save(updated);
        return toView(updated);
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

    private static UserProfileView toView(final UserRepository.UserRecord record) {
        return new UserProfileView(
                record.id(),
                record.email(),
                record.role(),
                record.provider(),
                record.name(),
                record.username(),
                record.avatarUrl()
        );
    }
}
