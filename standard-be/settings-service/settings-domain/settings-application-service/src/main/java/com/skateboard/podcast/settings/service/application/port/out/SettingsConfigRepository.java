package com.skateboard.podcast.settings.service.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface SettingsConfigRepository {

    Optional<SettingsConfigRecord> findById(long id);

    SettingsConfigRecord save(SettingsConfigRecord record);

    record SettingsConfigRecord(
            long id,
            String configJson,
            Instant updatedAt
    ) {}
}
