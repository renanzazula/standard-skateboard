package com.skateboard.podcast.feed.service.events.application.service;

import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.EventStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;
import com.skateboard.podcast.feed.service.events.application.dto.EventDetailsView;
import com.skateboard.podcast.feed.service.events.application.dto.EventSummaryView;
import com.skateboard.podcast.feed.service.events.application.dto.EventsVersion;
import com.skateboard.podcast.feed.service.events.application.port.in.PublicFeedEventsUseCase;
import com.skateboard.podcast.feed.service.events.application.port.out.EventRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class PublicFeedEventsService implements PublicFeedEventsUseCase {

    private final EventRepository eventRepository;

    public PublicFeedEventsService(final EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public List<EventSummaryView> listPublished(final int page, final int size) {
        if (page < 0) throw new ValidationException("page must be >= 0");
        if (size < 1 || size > 50) throw new ValidationException("size must be between 1 and 50");

        return eventRepository.findPublished(page, size).stream()
                .map(PublicFeedEventsService::toSummary)
                .toList();
    }

    @Override
    public Optional<EventDetailsView> getBySlug(final String slug) {
        final Slug slugValue = Slug.of(slug);
        return eventRepository.findBySlug(slugValue)
                .filter(event -> event.status() == EventStatus.PUBLISHED)
                .map(PublicFeedEventsService::toDetails);
    }

    @Override
    public EventsVersion getEventsVersion(final int page, final int size) {
        if (page < 0) throw new ValidationException("page must be >= 0");
        if (size < 1 || size > 50) throw new ValidationException("size must be between 1 and 50");

        final EventRepository.EventStats stats = eventRepository.fetchPublishedStats();
        final Instant lastUpdatedAt = stats.lastUpdatedAt() == null
                ? Instant.EPOCH
                : stats.lastUpdatedAt();
        final String etag = EventsVersion.buildEtag(lastUpdatedAt, stats.totalCount(), page, size);
        return new EventsVersion(etag, lastUpdatedAt);
    }

    private static EventSummaryView toSummary(final EventRepository.EventRecord event) {
        return new EventSummaryView(
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

    private static EventDetailsView toDetails(final EventRepository.EventRecord event) {
        return new EventDetailsView(
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


