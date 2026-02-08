package com.skateboard.podcast.feed.service.events.application.service;

import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.EventStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventDetailsView;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventSummaryView;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventsVersion;
import com.skateboard.podcast.feed.service.events.application.port.in.PublicFeedEventsUseCase;
import com.skateboard.podcast.feed.service.events.application.port.out.FeedEventRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class PublicFeedEventsService implements PublicFeedEventsUseCase {

    private final FeedEventRepository eventRepository;

    public PublicFeedEventsService(final FeedEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public List<FeedEventSummaryView> listPublished(final int page, final int size) {
        if (page < 0) throw new ValidationException("page must be >= 0");
        if (size < 1 || size > 50) throw new ValidationException("size must be between 1 and 50");

        return eventRepository.findPublished(page, size).stream()
                .map(PublicFeedEventsService::toSummary)
                .toList();
    }

    @Override
    public Optional<FeedEventDetailsView> getBySlug(final String slug) {
        final Slug slugValue = Slug.of(slug);
        return eventRepository.findBySlug(slugValue)
                .filter(event -> event.status() == EventStatus.PUBLISHED)
                .map(PublicFeedEventsService::toDetails);
    }

    @Override
    public FeedEventsVersion getFeedEventsVersion(final int page, final int size) {
        if (page < 0) throw new ValidationException("page must be >= 0");
        if (size < 1 || size > 50) throw new ValidationException("size must be between 1 and 50");

        final FeedEventRepository.FeedEventStats stats = eventRepository.fetchPublishedStats();
        final Instant lastUpdatedAt = stats.lastUpdatedAt() == null
                ? Instant.EPOCH
                : stats.lastUpdatedAt();
        final String etag = FeedEventsVersion.buildEtag(lastUpdatedAt, stats.totalCount(), page, size);
        return new FeedEventsVersion(etag, lastUpdatedAt);
    }

    private static FeedEventSummaryView toSummary(final FeedEventRepository.FeedEventRecord event) {
        return new FeedEventSummaryView(
                event.id(),
                event.title(),
                event.slug().value(),
                event.excerpt(),
                toTagStrings(event.tags()),
                event.status().name(),
                event.thumbnailJson(),
                event.startAt(),
                event.endAt(),
                event.timezone(),
                event.location(),
                event.ticketsUrl(),
                event.createdAt(),
                event.updatedAt(),
                event.createdBy()
        );
    }

    private static FeedEventDetailsView toDetails(final FeedEventRepository.FeedEventRecord event) {
        return new FeedEventDetailsView(
                event.id(),
                event.title(),
                event.slug().value(),
                event.excerpt(),
                toTagStrings(event.tags()),
                event.status().name(),
                event.thumbnailJson(),
                event.contentJson(),
                event.startAt(),
                event.endAt(),
                event.timezone(),
                event.location(),
                event.ticketsUrl(),
                event.createdAt(),
                event.updatedAt(),
                event.createdBy()
        );
    }

    private static List<String> toTagStrings(final List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(Tag::value)
                .toList();
    }
}


