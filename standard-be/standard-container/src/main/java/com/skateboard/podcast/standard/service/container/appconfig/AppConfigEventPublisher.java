package com.skateboard.podcast.standard.service.container.appconfig;

import java.time.Instant;

public interface AppConfigEventPublisher {
    void publishConfigUpdated(Instant updatedAt);
}
