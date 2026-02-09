package com.skateboard.podcast.iam.service.application.dto;

import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;

import java.util.UUID;

public record UserProfileView(
        UUID id,
        Email email,
        Role role,
        Provider provider,
        String name,
        String username,
        String avatarUrl
) {}
