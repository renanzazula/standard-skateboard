package com.skateboard.podcast.feed.service.events.application.port.out;

import com.skateboard.podcast.domain.valueobject.EventStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {

    Optional<EventRecord> findBySlug(Slug slug);

    Optional<EventRecord> findById(UUID id);

    List<EventRecord> findAll(int page, int size);

    List<EventRecord> findByStatus(EventStatus status, int page, int size);

    List<EventRecord> findPublished(int page, int size);

    EventStats fetchPublishedStats();

    EventRecord save(EventRecord event);

    void deleteById(UUID id);

    void deleteAll();

    record EventStats(Instant lastUpdatedAt, long totalCount) {}

    record EventRecord(
            UUID id,
            String title,
            Slug slug,
            String excerpt,
            List<Tag> tags,
            EventStatus status,
            String thumbnailJson,
            String contentJson,
            Instant startAt,
            Instant endAt,
            String timezone,
            String location,
            String ticketsUrl,
            UUID createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {}
}

