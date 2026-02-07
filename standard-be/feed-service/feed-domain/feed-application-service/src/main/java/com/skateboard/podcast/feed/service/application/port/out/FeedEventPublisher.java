package com.skateboard.podcast.feed.service.application.port.out;

import com.skateboard.podcast.feed.service.application.dto.PostEvent;

import java.time.Instant;

public interface FeedEventPublisher {
    void publishPostEvent(PostEvent event);

    void publishFeedUpdated(Instant updatedAt);
}
