package com.skateboard.podcast.feed.service.events.application.port.out;

import com.skateboard.podcast.feed.service.events.application.dto.EventEvent;

import java.time.Instant;

public interface EventsEventPublisher {
    void publishEventEvent(EventEvent event);

    void publishEventsUpdated(Instant updatedAt);
}

