package com.skateboard.podcast.feed.service.events.application.service;

import com.skateboard.podcast.domain.exception.NotFoundException;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.EventStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventDetailsView;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventEvent;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventSummaryView;
import com.skateboard.podcast.feed.service.events.application.dto.FeedEventImportCommand;
import com.skateboard.podcast.feed.service.events.application.port.in.AdminFeedEventsUseCase;
import com.skateboard.podcast.feed.service.events.application.port.out.FeedEventRepository;
import com.skateboard.podcast.feed.service.events.application.port.out.FeedEventsEventPublisher;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class AdminFeedEventsService implements AdminFeedEventsUseCase {

    private final FeedEventRepository eventRepository;
    private final FeedEventsEventPublisher eventsEventPublisher;

    public AdminFeedEventsService(
            final FeedEventRepository eventRepository,
            final FeedEventsEventPublisher eventsEventPublisher
    ) {
        this.eventRepository = eventRepository;
        this.eventsEventPublisher = eventsEventPublisher;
    }

    @Override
    public List<FeedEventSummaryView> list(final int page, final int size, final EventStatus status) {
        final List<FeedEventRepository.FeedEventRecord> records = status == null
                ? eventRepository.findAll(page, size)
                : eventRepository.findByStatus(status, page, size);
        return records.stream()
                .map(AdminFeedEventsService::toSummary)
                .toList();
    }

    @Override
    public Optional<FeedEventDetailsView> getById(final UUID id) {
        return eventRepository.findById(id).map(AdminFeedEventsService::toDetails);
    }

    @Override
    public FeedEventDetailsView createDraft(
            final String title,
            final String slug,
            final String excerpt,
            final List<String> tags,
            final String thumbnailJson,
            final String contentJson,
            final Instant startAt,
            final Instant endAt,
            final String timezone,
            final String location,
            final String ticketsUrl,
            final UUID createdBy
    ) {
        validateBasics(title, contentJson);
        validateDates(startAt, endAt);

        final Slug slugValue = Slug.of(slug);
        final List<Tag> tagValues = toTagValues(tags);
        final Instant now = Instant.now();
        final UUID eventId = UUID.randomUUID();

        final var record = new FeedEventRepository.FeedEventRecord(
                eventId,
                title.trim(),
                slugValue,
                excerpt,
                tagValues,
                EventStatus.DRAFT,
                thumbnailJson,
                contentJson,
                startAt,
                endAt,
                timezone,
                location,
                ticketsUrl,
                createdBy,
                now,
                now
        );
        final var saved = eventRepository.save(record);
        return toDetails(saved);
    }

    @Override
    public FeedEventDetailsView updateById(
            final UUID id,
            final String title,
            final String slug,
            final String excerpt,
            final List<String> tags,
            final String thumbnailJson,
            final String contentJson,
            final Instant startAt,
            final Instant endAt,
            final String timezone,
            final String location,
            final String ticketsUrl
    ) {
        validateBasics(title, contentJson);
        validateDates(startAt, endAt);

        final var existing = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("event not found"));

        final Slug slugValue = Slug.of(slug);
        final List<Tag> tagValues = toTagValues(tags);
        final Instant now = Instant.now();

        final var updated = new FeedEventRepository.FeedEventRecord(
                existing.id(),
                title.trim(),
                slugValue,
                excerpt,
                tagValues,
                existing.status(),
                thumbnailJson,
                contentJson,
                startAt,
                endAt,
                timezone,
                location,
                ticketsUrl,
                existing.createdBy(),
                existing.createdAt(),
                now
        );
        final var saved = eventRepository.save(updated);
        if (saved.status() == EventStatus.PUBLISHED) {
            publishFeedEventEvent("event.updated", saved);
        }
        return toDetails(saved);
    }

    @Override
    public FeedEventDetailsView publishById(final UUID id) {
        final var existing = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("event not found"));

        final Instant now = Instant.now();
        final var updated = new FeedEventRepository.FeedEventRecord(
                existing.id(),
                existing.title(),
                existing.slug(),
                existing.excerpt(),
                existing.tags(),
                EventStatus.PUBLISHED,
                existing.thumbnailJson(),
                existing.contentJson(),
                existing.startAt(),
                existing.endAt(),
                existing.timezone(),
                existing.location(),
                existing.ticketsUrl(),
                existing.createdBy(),
                existing.createdAt(),
                now
        );
        final var saved = eventRepository.save(updated);
        publishFeedEventEvent("event.published", saved);
        return toDetails(saved);
    }

    @Override
    public void deleteById(final UUID id) {
        final var existing = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("event not found"));
        eventRepository.deleteById(id);
        if (existing.status() == EventStatus.PUBLISHED) {
            eventsEventPublisher.publishFeedEventEvent(new FeedEventEvent(
                    "event.deleted",
                    existing.id(),
                    existing.slug().value(),
                    Instant.now()
            ));
        }
    }

    @Override
    public List<FeedEventSummaryView> importEvents(final List<FeedEventImportCommand> items, final UUID createdBy) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        final Set<String> seenSlugs = new HashSet<>();
        final List<FeedEventSummaryView> results = items.stream()
                .map(item -> importOne(item, createdBy, seenSlugs))
                .toList();
        eventsEventPublisher.publishEventsUpdated(Instant.now());
        return results;
    }

    @Override
    public void resetAll() {
        eventRepository.deleteAll();
        eventsEventPublisher.publishEventsUpdated(Instant.now());
    }

    private static void validateBasics(final String title, final String contentJson) {
        if (title == null || title.isBlank()) throw new ValidationException("title cannot be blank");
        if (contentJson == null || contentJson.isBlank()) throw new ValidationException("content cannot be blank");
    }

    private static void validateDates(final Instant startAt, final Instant endAt) {
        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw new ValidationException("endAt cannot be before startAt");
        }
    }

    private static List<Tag> toTagValues(final List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(Tag::of)
                .toList();
    }

    private FeedEventSummaryView importOne(
            final FeedEventImportCommand item,
            final UUID createdBy,
            final Set<String> seenSlugs
    ) {
        if (item == null) {
            throw new ValidationException("import item cannot be null");
        }
        if (item.title() == null || item.title().isBlank()) {
            throw new ValidationException("title cannot be blank");
        }
        if (item.contentJson() == null || item.contentJson().isBlank()) {
            throw new ValidationException("content cannot be blank");
        }
        validateDates(item.startAt(), item.endAt());

        final String baseSlug = item.slug() == null || item.slug().isBlank() ? item.title() : item.slug();
        final String slugValue = uniqueSlug(baseSlug, seenSlugs);
        final Instant now = Instant.now();

        final var record = new FeedEventRepository.FeedEventRecord(
                UUID.randomUUID(),
                item.title().trim(),
                Slug.of(slugValue),
                item.excerpt(),
                toTagValues(item.tags()),
                EventStatus.PUBLISHED,
                item.thumbnailJson(),
                item.contentJson(),
                item.startAt(),
                item.endAt(),
                item.timezone(),
                item.location(),
                item.ticketsUrl(),
                createdBy,
                now,
                now
        );
        final var saved = eventRepository.save(record);
        return toSummary(saved);
    }

    private String uniqueSlug(final String titleOrSlug, final Set<String> seenSlugs) {
        final String base = Slug.normalize(titleOrSlug);
        String candidate = base;
        int counter = 1;
        while (seenSlugs.contains(candidate)
                || eventRepository.findBySlug(Slug.of(candidate)).isPresent()) {
            candidate = base + "-" + counter;
            counter += 1;
        }
        seenSlugs.add(candidate);
        return candidate;
    }

    private static FeedEventDetailsView toDetails(final FeedEventRepository.FeedEventRecord event) {
        return new FeedEventDetailsView(
                event.id(),
                event.title(),
                event.slug().value(),
                event.excerpt(),
                event.tags().stream().map(Tag::value).toList(),
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

    private static FeedEventSummaryView toSummary(final FeedEventRepository.FeedEventRecord event) {
        return new FeedEventSummaryView(
                event.id(),
                event.title(),
                event.slug().value(),
                event.excerpt(),
                event.tags().stream().map(Tag::value).toList(),
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

    private void publishFeedEventEvent(final String type, final FeedEventRepository.FeedEventRecord event) {
        eventsEventPublisher.publishFeedEventEvent(new FeedEventEvent(
                type,
                event.id(),
                event.slug().value(),
                event.updatedAt()
        ));
    }
}


