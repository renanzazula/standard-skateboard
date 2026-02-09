package com.skateboard.podcast.iam.service.application.adapter.in.rest;

import com.skateboard.podcast.iam.service.application.dto.UserProfileUpdateCommand;
import com.skateboard.podcast.iam.service.application.dto.UserProfileView;
import com.skateboard.podcast.standardbe.api.model.Provider;
import com.skateboard.podcast.standardbe.api.model.UserProfile;
import com.skateboard.podcast.standardbe.api.model.UserProfileUpdateRequest;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class UserProfileApiMapper {

    public UserProfile toApi(final UserProfileView view) {
        if (view == null) {
            return null;
        }
        return new UserProfile()
                .id(view.id())
                .email(view.email().value())
                .role(com.skateboard.podcast.standardbe.api.model.Role.valueOf(view.role().name()))
                .provider(Provider.valueOf(view.provider().name()))
                .name(view.name())
                .username(view.username())
                .avatarUrl(toUri(view.avatarUrl()));
    }

    public UserProfileUpdateCommand toCommand(final UserProfileUpdateRequest request) {
        if (request == null) {
            return null;
        }
        return new UserProfileUpdateCommand(
                request.getName(),
                request.getUsername(),
                toStringValue(request.getAvatarUrl())
        );
    }

    private static URI toUri(final String value) {
        return value == null ? null : URI.create(value);
    }

    private static String toStringValue(final URI value) {
        return value == null ? null : value.toString();
    }
}
