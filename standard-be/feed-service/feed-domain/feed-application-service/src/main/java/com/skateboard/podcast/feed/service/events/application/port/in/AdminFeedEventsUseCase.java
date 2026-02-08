package com.skateboard.podcast.feed.service.events.application.port.in;

import com.skateboard.podcast.feed.service.events.application.dto.EventDetailsView;
import com.skateboard.podcast.feed.service.events.application.dto.EventSummaryView;
import com.skateboard.podcast.feed.service.events.application.dto.ImportEventCommand;
import com.skateboard.podcast.domain.valueobject.EventStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminFeedEventsUseCase {
    List<EventSummaryView> list(int page, int size, EventStatus status);

    Optional<EventDetailsView> getById(UUID id);

    EventDetailsView createDraft(
            String title,
            String slug,
            String excerpt,
            List<String> tags,
            String thumbnailJson,
            String contentJson,
            Instant startAt,
            Instant endAt,
            String timezone,
            String location,
            String ticketsUrl,
            UUID createdBy
    );

    EventDetailsView updateById(
            UUID id,
            String title,
            String slug,
            String excerpt,
            List<String> tags,
            String thumbnailJson,
            String contentJson,
            Instant startAt,
            Instant endAt,
            String timezone,
            String location,
            String ticketsUrl
    );

    EventDetailsView publishById(UUID id);

    void deleteById(UUID id);

    List<EventSummaryView> importEvents(List<ImportEventCommand> items, UUID createdBy);

    void resetAll();
}


