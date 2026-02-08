package com.skateboard.podcast.feed.service.events.application.port.out;

import com.skateboard.podcast.domain.valueobject.EventStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedEventRepository {

    Optional<FeedEventRecord> findBySlug(Slug slug);

    Optional<FeedEventRecord> findById(UUID id);

    List<FeedEventRecord> findAll(int page, int size);

    List<FeedEventRecord> findByStatus(EventStatus status, int page, int size);

    List<FeedEventRecord> findPublished(int page, int size);

    FeedEventStats fetchPublishedStats();

    FeedEventRecord save(FeedEventRecord event);

    void deleteById(UUID id);

    void deleteAll();

    record FeedEventStats(Instant lastUpdatedAt, long totalCount) {}

    record FeedEventRecord(
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

