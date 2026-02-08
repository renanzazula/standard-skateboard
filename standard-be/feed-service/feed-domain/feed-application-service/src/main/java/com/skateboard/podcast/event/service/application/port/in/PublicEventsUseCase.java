package com.skateboard.podcast.event.service.application.port.in;

import com.skateboard.podcast.event.service.application.dto.EventDetailsView;
import com.skateboard.podcast.event.service.application.dto.EventSummaryView;
import com.skateboard.podcast.event.service.application.dto.EventsVersion;

import java.util.List;
import java.util.Optional;

public interface PublicEventsUseCase {
    List<EventSummaryView> listPublished(int page, int size);

    Optional<EventDetailsView> getBySlug(String slug);

    EventsVersion getEventsVersion(int page, int size);
}
