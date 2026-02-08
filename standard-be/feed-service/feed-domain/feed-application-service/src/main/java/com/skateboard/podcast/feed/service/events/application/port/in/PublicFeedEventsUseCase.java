package com.skateboard.podcast.feed.service.events.application.port.in;

import com.skateboard.podcast.feed.service.events.application.dto.FeedEventDetailsView;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventSummaryView;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventsVersion;

import java.util.List;
import java.util.Optional;

public interface PublicFeedEventsUseCase {
    List<FeedEventSummaryView> listPublished(int page, int size);

    Optional<FeedEventDetailsView> getBySlug(String slug);

    FeedEventsVersion getFeedEventsVersion(int page, int size);
}


