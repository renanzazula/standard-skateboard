package com.skateboard.podcast.event.service.application.port.out;

import com.skateboard.podcast.event.service.application.dto.EventEvent;

import java.time.Instant;

public interface EventsEventPublisher {
    void publishEventEvent(EventEvent event);

    void publishEventsUpdated(Instant updatedAt);
}
