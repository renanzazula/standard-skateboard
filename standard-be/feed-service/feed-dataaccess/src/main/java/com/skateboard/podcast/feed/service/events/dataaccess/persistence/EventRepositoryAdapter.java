package com.skateboard.podcast.feed.service.events.dataaccess.persistence;

import com.skateboard.podcast.domain.valueobject.EventStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;
import com.skateboard.podcast.feed.service.events.application.port.out.EventRepository;
import com.skateboard.podcast.feed.service.events.dataaccess.persistence.jpa.EventJpaEntity;
import com.skateboard.podcast.feed.service.events.dataaccess.persistence.jpa.SpringDataEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class EventRepositoryAdapter implements EventRepository {

    private final SpringDataEventRepository repo;

    public EventRepositoryAdapter(final SpringDataEventRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<EventRecord> findBySlug(final Slug slug) {
        return repo.findBySlug(slug.value()).map(EventRepositoryAdapter::toRecord);
    }

    @Override
    public Optional<EventRecord> findById(final java.util.UUID id) {
        return repo.findById(id).map(EventRepositoryAdapter::toRecord);
    }

    @Override
    public List<EventRecord> findAll(final int page, final int size) {
        return repo.findAllByOrderByUpdatedAtDesc(PageRequest.of(page, size))
                .map(EventRepositoryAdapter::toRecord)
                .getContent();
    }

    @Override
    public List<EventRecord> findByStatus(final EventStatus status, final int page, final int size) {
        return repo.findByStatusOrderByUpdatedAtDesc(status.name(), PageRequest.of(page, size))
                .map(EventRepositoryAdapter::toRecord)
                .getContent();
    }

    @Override
    public List<EventRecord> findPublished(final int page, final int size) {
        return repo.findByStatusOrderByStartAtDesc(
                        EventStatus.PUBLISHED.name(),
                        PageRequest.of(page, size)
                )
                .map(EventRepositoryAdapter::toRecord)
                .getContent();
    }

    @Override
    public EventStats fetchPublishedStats() {
        final Instant lastUpdatedAt = repo.findMaxUpdatedAtByStatus(EventStatus.PUBLISHED.name());
        final long totalCount = repo.countByStatus(EventStatus.PUBLISHED.name());
        return new EventStats(lastUpdatedAt, totalCount);
    }

    @Override
    public EventRecord save(final EventRecord event) {
        final EventJpaEntity e = new EventJpaEntity();
        e.setId(event.id());
        e.setTitle(event.title());
        e.setSlug(event.slug().value());
        e.setExcerpt(event.excerpt());
        e.setTags(toTagArray(event.tags()));
        e.setStatus(event.status().name());
        e.setThumbnailJson(event.thumbnailJson());
        e.setContentJson(event.contentJson());
        e.setStartAt(event.startAt());
        e.setEndAt(event.endAt());
        e.setTimezone(event.timezone());
        e.setLocation(event.location());
        e.setTicketsUrl(event.ticketsUrl());
        e.setCreatedBy(event.createdBy());
        e.setCreatedAt(event.createdAt());
        e.setUpdatedAt(event.updatedAt());
        repo.save(e);
        return event;
    }

    @Override
    public void deleteById(final java.util.UUID id) {
        repo.deleteById(id);
    }

    @Override
    public void deleteAll() {
        repo.deleteAll();
    }

    private static EventRecord toRecord(final EventJpaEntity e) {
        return new EventRecord(
                e.getId(),
                e.getTitle(),
                Slug.of(e.getSlug()),
                e.getExcerpt(),
                toTags(e.getTags()),
                EventStatus.from(e.getStatus()),
                e.getThumbnailJson(),
                e.getContentJson(),
                e.getStartAt(),
                e.getEndAt(),
                e.getTimezone(),
                e.getLocation(),
                e.getTicketsUrl(),
                e.getCreatedBy(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private static List<Tag> toTags(final String[] tags) {
        if (tags == null || tags.length == 0) {
            return List.of();
        }
        return Arrays.stream(tags)
                .map(Tag::of)
                .toList();
    }

    private static String[] toTagArray(final List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }
        return tags.stream()
                .map(Tag::value)
                .toArray(String[]::new);
    }
}

