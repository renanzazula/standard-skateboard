package com.skateboard.podcast.feed.service.events.application.port.in;

import com.skateboard.podcast.feed.service.events.application.dto.FeedEventDetailsView;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventSummaryView;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventImportCommand;
import com.skateboard.podcast.domain.valueobject.EventStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminFeedEventsUseCase {
    List<FeedEventSummaryView> list(int page, int size, EventStatus status);

    Optional<FeedEventDetailsView> getById(UUID id);

    FeedEventDetailsView createDraft(
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

    FeedEventDetailsView updateById(
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

    FeedEventDetailsView publishById(UUID id);

    void deleteById(UUID id);

    List<FeedEventSummaryView> importEvents(List<FeedEventImportCommand> items, UUID createdBy);

    void resetAll();
}


