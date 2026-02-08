package com.skateboard.podcast.feed.service.application.service;

import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.EventStatus;
import com.skateboard.podcast.domain.valueobject.PostStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;
import com.skateboard.podcast.feed.service.application.dto.FeedVersion;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import com.skateboard.podcast.feed.service.events.application.port.out.FeedEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PublicFeedServiceTest {

    @Test
    void listPublishedValidatesPageAndSize() {
        final PublicFeedService service = new PublicFeedService(
                new InMemoryPostRepository(List.of()),
                new InMemoryFeedEventRepository(List.of())
        );

        assertThrows(ValidationException.class, () -> service.listPublished(-1, 10));
        assertThrows(ValidationException.class, () -> service.listPublished(0, 0));
        assertThrows(ValidationException.class, () -> service.listPublished(0, 51));
    }

    @Test
    void listPublishedMergesBySortTimestamp() {
        final PostRepository.PostRecord postLatest = postRecord(
                "post-10",
                Instant.parse("2024-01-10T00:00:00Z")
        );
        final PostRepository.PostRecord postOlder = postRecord(
                "post-07",
                Instant.parse("2024-01-07T00:00:00Z")
        );

        final FeedEventRepository.FeedEventRecord eventNewest = eventRecord(
                "event-09",
                Instant.parse("2024-01-09T00:00:00Z")
        );
        final FeedEventRepository.FeedEventRecord eventOlder = eventRecord(
                "event-08",
                Instant.parse("2024-01-08T00:00:00Z")
        );

        final PublicFeedService service = new PublicFeedService(
                new InMemoryPostRepository(List.of(postLatest, postOlder)),
                new InMemoryFeedEventRepository(List.of(eventNewest, eventOlder))
        );

        final var page0 = service.listPublished(0, 2);
        assertEquals(2, page0.size());
        assertEquals("post-10", page0.get(0).slug());
        assertEquals("event-09", page0.get(1).slug());

        final var page1 = service.listPublished(1, 2);
        assertEquals(2, page1.size());
        assertEquals("event-08", page1.get(0).slug());
        assertEquals("post-07", page1.get(1).slug());
    }

    @Test
    void getFeedVersionUsesMaxUpdatedAtAndTotalCount() {
        final PostRepository.PostRecord postLatest = postRecord(
                "post-10",
                Instant.parse("2024-01-10T00:00:00Z")
        );
        final PostRepository.PostRecord postOlder = postRecord(
                "post-07",
                Instant.parse("2024-01-07T00:00:00Z")
        );
        final FeedEventRepository.FeedEventRecord eventNewest = eventRecord(
                "event-09",
                Instant.parse("2024-02-01T00:00:00Z")
        );

        final PublicFeedService service = new PublicFeedService(
                new InMemoryPostRepository(List.of(postLatest, postOlder)),
                new InMemoryFeedEventRepository(List.of(eventNewest))
        );

        final FeedVersion version = service.getFeedVersion(0, 20);
        assertEquals(Instant.parse("2024-02-01T00:00:00Z"), version.lastUpdatedAt());
        assertEquals(
                FeedVersion.buildEtag(version.lastUpdatedAt(), 3, 0, 20),
                version.etag()
        );
    }

    private static PostRepository.PostRecord postRecord(final String slug, final Instant publishedAt) {
        return new PostRepository.PostRecord(
                UUID.randomUUID(),
                "Title",
                Slug.of(slug),
                "Excerpt",
                List.of(Tag.of("tech")),
                PostStatus.PUBLISHED,
                null,
                "[]",
                UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"),
                publishedAt,
                publishedAt
        );
    }

    private static FeedEventRepository.FeedEventRecord eventRecord(final String slug, final Instant startAt) {
        return new FeedEventRepository.FeedEventRecord(
                UUID.randomUUID(),
                "Event",
                Slug.of(slug),
                "Excerpt",
                List.of(Tag.of("street")),
                EventStatus.PUBLISHED,
                null,
                "[]",
                startAt,
                startAt.plusSeconds(3600),
                "UTC",
                "Plaza",
                "https://tickets.example.com",
                UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"),
                startAt
        );
    }

    private static final class InMemoryPostRepository implements PostRepository {
        private final List<PostRecord> records;

        private InMemoryPostRepository(final List<PostRecord> records) {
            this.records = new ArrayList<>(records);
        }

        @Override
        public Optional<PostRecord> findBySlug(final Slug slug) {
            return records.stream().filter(record -> record.slug().equals(slug)).findFirst();
        }

        @Override
        public Optional<PostRecord> findById(final UUID id) {
            return records.stream().filter(record -> record.id().equals(id)).findFirst();
        }

        @Override
        public List<PostRecord> findAll(final int page, final int size) {
            return List.copyOf(records);
        }

        @Override
        public List<PostRecord> findByStatus(final PostStatus status, final int page, final int size) {
            return records.stream().filter(record -> record.status() == status).toList();
        }

        @Override
        public List<PostRecord> findPublished(final int page, final int size) {
            final List<PostRecord> published = records.stream()
                    .filter(record -> record.status() == PostStatus.PUBLISHED)
                    .sorted(Comparator.comparing(PostRecord::publishedAt).reversed())
                    .toList();
            return slice(published, page, size);
        }

        @Override
        public FeedStats fetchPublishedFeedStats() {
            final List<PostRecord> published = records.stream()
                    .filter(record -> record.status() == PostStatus.PUBLISHED)
                    .toList();
            final Instant lastUpdatedAt = published.stream()
                    .map(PostRecord::updatedAt)
                    .max(Instant::compareTo)
                    .orElse(null);
            return new FeedStats(lastUpdatedAt, published.size());
        }

        @Override
        public PostRecord save(final PostRecord post) {
            records.add(post);
            return post;
        }

        @Override
        public void deleteById(final UUID id) {
            records.removeIf(record -> record.id().equals(id));
        }

        @Override
        public void deleteAll() {
            records.clear();
        }
    }

    private static final class InMemoryFeedEventRepository implements FeedEventRepository {
        private final List<FeedEventRecord> records;

        private InMemoryFeedEventRepository(final List<FeedEventRecord> records) {
            this.records = new ArrayList<>(records);
        }

        @Override
        public Optional<FeedEventRecord> findBySlug(final Slug slug) {
            return records.stream().filter(record -> record.slug().equals(slug)).findFirst();
        }

        @Override
        public Optional<FeedEventRecord> findById(final UUID id) {
            return records.stream().filter(record -> record.id().equals(id)).findFirst();
        }

        @Override
        public List<FeedEventRecord> findAll(final int page, final int size) {
            return List.copyOf(records);
        }

        @Override
        public List<FeedEventRecord> findByStatus(final EventStatus status, final int page, final int size) {
            return records.stream().filter(record -> record.status() == status).toList();
        }

        @Override
        public List<FeedEventRecord> findPublished(final int page, final int size) {
            final List<FeedEventRecord> published = records.stream()
                    .filter(record -> record.status() == EventStatus.PUBLISHED)
                    .sorted(Comparator.comparing(FeedEventRecord::startAt).reversed())
                    .toList();
            return slice(published, page, size);
        }

        @Override
        public FeedEventStats fetchPublishedStats() {
            final List<FeedEventRecord> published = records.stream()
                    .filter(record -> record.status() == EventStatus.PUBLISHED)
                    .toList();
            final Instant lastUpdatedAt = published.stream()
                    .map(FeedEventRecord::updatedAt)
                    .max(Instant::compareTo)
                    .orElse(null);
            return new FeedEventStats(lastUpdatedAt, published.size());
        }

        @Override
        public FeedEventRecord save(final FeedEventRecord event) {
            records.add(event);
            return event;
        }

        @Override
        public void deleteById(final UUID id) {
            records.removeIf(record -> record.id().equals(id));
        }

        @Override
        public void deleteAll() {
            records.clear();
        }
    }

    private static <T> List<T> slice(final List<T> items, final int page, final int size) {
        if (size <= 0) {
            return List.of();
        }
        final int from = page * size;
        if (from >= items.size()) {
            return List.of();
        }
        final int to = Math.min(from + size, items.size());
        return items.subList(from, to);
    }
}
