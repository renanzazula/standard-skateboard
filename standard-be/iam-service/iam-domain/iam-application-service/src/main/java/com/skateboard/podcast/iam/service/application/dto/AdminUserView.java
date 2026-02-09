package com.skateboard.podcast.iam.service.application.dto;

import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminUserView(
        UUID id,
        Email email,
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
