package com.skateboard.podcast.iam.service.application.adapter.in.rest;

import com.skateboard.podcast.iam.service.application.dto.AuthResult;
import com.skateboard.podcast.standardbe.api.model.AuthResponse;
import com.skateboard.podcast.standardbe.api.model.AuthTokens;
import com.skateboard.podcast.standardbe.api.model.Provider;
import com.skateboard.podcast.standardbe.api.model.Role;
import com.skateboard.podcast.standardbe.api.model.UserSummary;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class AuthMapper {

    public AuthResponse toAuthResponse(final AuthResult result) {
        final AuthTokens tokens = new AuthTokens()
                .accessToken(result.accessToken())
                .expiresInSeconds((int) result.expiresInSeconds())
                .refreshToken(result.refreshToken())
                .refreshExpiresInSeconds((int) result.refreshExpiresInSeconds());

        final UserSummary user = new UserSummary()
                .id(result.userId().value())
                .email(result.email().value())
                .role(Role.valueOf(result.role().name()))
                .provider(Provider.valueOf(result.provider().name()))
                .name(result.name())
                .avatarUrl(toUri(result.avatarUrl()));

        return new AuthResponse()
                .tokens(tokens)
                .user(user);
    }

    private static URI toUri(final String value) {
        return value == null ? null : URI.create(value);
    }
}
