package com.skateboard.podcast.appconfig.service.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface NavigationConfigRepository {

    Optional<NavigationConfigRecord> findById(long id);

    NavigationConfigRecord save(NavigationConfigRecord record);

    record NavigationConfigRecord(
            long id,
            String configJson,
            Instant updatedAt
    ) {}
}
