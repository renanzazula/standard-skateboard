package com.skateboard.podcast.iam.service.application.dto;

public record UserProfileUpdateCommand(
        String name,
        String username,
        String avatarUrl
) {}
