package com.skateboard.podcast.iam.service.application.adapter.in.rest;

import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserStatus;
import com.skateboard.podcast.iam.service.application.dto.AdminUserUpdateCommand;
import com.skateboard.podcast.iam.service.application.dto.AdminUserView;
import com.skateboard.podcast.standardbe.api.model.AdminUser;
import com.skateboard.podcast.standardbe.api.model.AdminUserStatusUpdateRequest;
import com.skateboard.podcast.standardbe.api.model.AdminUserUpdateRequest;
import com.skateboard.podcast.standardbe.api.model.Provider;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class AdminUsersApiMapper {

    public AdminUser toApi(final AdminUserView view) {
        if (view == null) {
            return null;
        }
        return new AdminUser()
                .id(view.id())
                .email(view.email().value())
                .role(com.skateboard.podcast.standardbe.api.model.Role.valueOf(view.role().name()))
                .provider(Provider.valueOf(view.provider().name()))
                .status(com.skateboard.podcast.standardbe.api.model.UserStatus.valueOf(view.status().name()))
                .name(view.name())
                .username(view.username())
                .avatarUrl(toUri(view.avatarUrl()))
                .createdAt(toOffsetDateTime(view.createdAt()))
                .updatedAt(toOffsetDateTime(view.updatedAt()))
                .lastLoginAt(toOffsetDateTime(view.lastLoginAt()));
    }

    public AdminUserUpdateCommand toCommand(final AdminUserUpdateRequest request) {
        if (request == null) {
            return null;
        }
        final Role role = request.getRole() == null ? null : Role.from(request.getRole().name());
        return new AdminUserUpdateCommand(
                request.getName(),
                request.getUsername(),
                role
        );
    }

    public UserStatus toStatus(final AdminUserStatusUpdateRequest request) {
        if (request == null || request.getStatus() == null) {
            return null;
        }
        return UserStatus.from(request.getStatus().name());
    }

    private static OffsetDateTime toOffsetDateTime(final Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atOffset(ZoneOffset.UTC);
    }

    private static URI toUri(final String value) {
        return value == null ? null : URI.create(value);
    }
}
