package com.skateboard.podcast.iam.service.application.port.in;

import com.skateboard.podcast.iam.service.application.dto.UserProfileUpdateCommand;
import com.skateboard.podcast.iam.service.application.dto.UserProfileView;

import java.util.UUID;

public interface UserProfileUseCase {

    UserProfileView get(UUID userId);

    UserProfileView update(UUID userId, UserProfileUpdateCommand command);
}
