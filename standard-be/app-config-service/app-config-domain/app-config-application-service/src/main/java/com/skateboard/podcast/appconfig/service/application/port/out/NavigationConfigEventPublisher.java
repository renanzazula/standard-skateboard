package com.skateboard.podcast.appconfig.service.application.port.out;

import java.time.Instant;

public interface NavigationConfigEventPublisher {
    void publishNavigationUpdated(Instant updatedAt);
}
