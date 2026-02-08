package com.skateboard.podcast.feed.service.events.application.port.out;

import com.skateboard.podcast.feed.service.events.application.dto.FeedEventEvent;

import java.time.Instant;

public interface FeedEventsEventPublisher {
    void publishFeedEventEvent(FeedEventEvent event);

    void publishEventsUpdated(Instant updatedAt);
}

