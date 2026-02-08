package com.skateboard.podcast.appconfig.service.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface AppConfigRepository {

    Optional<AppConfigRecord> findById(long id);

    AppConfigRecord save(AppConfigRecord record);

    record AppConfigRecord(
            long id,
            boolean socialLoginEnabled,
            int postsPerPage,
            boolean splashEnabled,
            String splashMediaType,
            String splashMediaUrl,
            int splashDuration,
            boolean splashShowCloseButton,
            int splashCloseButtonDelay,
            Instant updatedAt
    ) {}
}
