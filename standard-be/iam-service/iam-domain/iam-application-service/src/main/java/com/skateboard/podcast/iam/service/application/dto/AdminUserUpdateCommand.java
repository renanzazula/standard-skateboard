package com.skateboard.podcast.iam.service.application.dto;

import com.skateboard.podcast.domain.valueobject.Role;

public record AdminUserUpdateCommand(
        String name,
        String username,
        Role role
) {}
