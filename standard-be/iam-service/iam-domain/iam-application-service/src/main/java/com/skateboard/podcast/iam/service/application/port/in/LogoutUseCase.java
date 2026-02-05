package com.skateboard.podcast.iam.service.application.port.in;

import java.util.UUID;

public interface LogoutUseCase {
    void logoutAllForUser(UUID userId);
}
