package com.skateboard.podcast.event.service.application.port.in;

import com.skateboard.podcast.event.service.application.dto.EventDetailsView;
import com.skateboard.podcast.event.service.application.dto.EventSummaryView;
import com.skateboard.podcast.event.service.application.dto.ImportEventCommand;
import com.skateboard.podcast.domain.valueobject.EventStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminEventUseCase {
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
