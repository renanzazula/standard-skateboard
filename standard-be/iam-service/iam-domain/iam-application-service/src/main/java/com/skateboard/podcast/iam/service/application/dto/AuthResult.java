package com.skateboard.podcast.iam.service.application.dto;

import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserId;

public record AuthResult(
        String accessToken,
        long expiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds,
        UserId userId,
        Email email,
        Role role,
        Provider provider
) {}
