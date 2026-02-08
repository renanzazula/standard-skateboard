package com.skateboard.podcast.feed.service.events.application.port.in;

import com.skateboard.podcast.feed.service.events.application.dto.EventDetailsView;
import com.skateboard.podcast.feed.service.events.application.dto.EventSummaryView;
import com.skateboard.podcast.feed.service.events.application.dto.EventsVersion;

import java.util.List;
import java.util.Optional;

public interface PublicFeedEventsUseCase {
    List<EventSummaryView> listPublished(int page, int size);

    Optional<EventDetailsView> getBySlug(String slug);

    EventsVersion getEventsVersion(int page, int size);
}


