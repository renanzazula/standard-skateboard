package com.skateboard.podcast.feed.service.application.service;

import com.skateboard.podcast.domain.exception.NotFoundException;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.PostStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;
import com.skateboard.podcast.feed.service.application.dto.ImportPostCommand;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import com.skateboard.podcast.feed.service.application.port.out.FeedEventPublisher;
import com.skateboard.podcast.feed.service.application.dto.PostEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminPostsServiceTest {

    @Test
    void createDraftRequiresTitleAndContent() {
        final AdminPostsService service = new AdminPostsService(
                new InMemoryPostRepository(),
                new NoOpFeedEventPublisher()
        );

        assertThrows(ValidationException.class, () -> service.createDraft(
                " ",
                "slug",
                null,
                List.of(),
                null,
                "[]",
                UUID.randomUUID()
        ));

        assertThrows(ValidationException.class, () -> service.createDraft(
                "Title",
                "slug",
                null,
                List.of(),
                null,
                " ",
                UUID.randomUUID()
        ));
    }

    @Test
    void updateByIdRejectsMissingPost() {
        final AdminPostsService service = new AdminPostsService(
                new InMemoryPostRepository(),
                new NoOpFeedEventPublisher()
        );

        assertThrows(NotFoundException.class, () -> service.updateById(
                UUID.randomUUID(),
                "Title",
                "slug",
                null,
                List.of(),
                null,
                "[]"
        ));
    }

    @Test
    void publishByIdSetsPublishedAtWhenMissing() {
        final InMemoryPostRepository repo = new InMemoryPostRepository();
        final AdminPostsService service = new AdminPostsService(repo, new NoOpFeedEventPublisher());

        final PostRepository.PostRecord draft = new PostRepository.PostRecord(
                UUID.randomUUID(),
                "Title",
                Slug.of("draft"),
                null,
                List.of(Tag.of("tech")),
                PostStatus.DRAFT,
                null,
                "[]",
                UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
        repo.save(draft);

        final var published = service.publishById(draft.id());

        assertEquals(PostStatus.PUBLISHED.name(), published.status());
        assertNotNull(published.publishedAt());
    }

    @Test
    void updateByIdPreservesAuthorAndCreatedAt() {
        final InMemoryPostRepository repo = new InMemoryPostRepository();
        final AdminPostsService service = new AdminPostsService(repo, new NoOpFeedEventPublisher());

        final UUID authorId = UUID.randomUUID();
        final Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        final PostRepository.PostRecord draft = new PostRepository.PostRecord(
                UUID.randomUUID(),
                "Title",
                Slug.of("draft"),
                null,
                List.of(Tag.of("tech")),
                PostStatus.DRAFT,
                null,
                "[]",
                authorId,
                createdAt,
                createdAt,
                null
        );
        repo.save(draft);

        final var updated = service.updateById(
                draft.id(),
                "Updated",
                "updated",
                "Excerpt",
                List.of("news"),
                null,
                "[]"
        );

        assertEquals(authorId, updated.authorId());
        assertEquals(createdAt, repo.findById(draft.id()).orElseThrow().createdAt());
    }

    @Test
    void publishByIdEmitsEvent() {
        final InMemoryPostRepository repo = new InMemoryPostRepository();
        final RecordingFeedEventPublisher publisher = new RecordingFeedEventPublisher();
        final AdminPostsService service = new AdminPostsService(repo, publisher);

        final PostRepository.PostRecord draft = new PostRepository.PostRecord(
                UUID.randomUUID(),
                "Title",
                Slug.of("draft"),
                null,
                List.of(Tag.of("tech")),
                PostStatus.DRAFT,
                null,
                "[]",
                UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
        repo.save(draft);

        service.publishById(draft.id());

        assertEquals(1, publisher.postEvents.size());
        assertEquals("post.published", publisher.postEvents.get(0).type());
        assertEquals(draft.id(), publisher.postEvents.get(0).postId());
        assertEquals(draft.slug().value(), publisher.postEvents.get(0).slug());
    }

    @Test
    void updatePublishedEmitsEvent() {
        final InMemoryPostRepository repo = new InMemoryPostRepository();
        final RecordingFeedEventPublisher publisher = new RecordingFeedEventPublisher();
        final AdminPostsService service = new AdminPostsService(repo, publisher);

        final PostRepository.PostRecord published = new PostRepository.PostRecord(
                UUID.randomUUID(),
                "Title",
                Slug.of("published"),
                null,
                List.of(Tag.of("tech")),
                PostStatus.PUBLISHED,
                null,
                "[]",
                UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        repo.save(published);

        service.updateById(
                published.id(),
                "Updated",
                "updated",
                null,
                List.of("news"),
                null,
                "[]"
        );

        assertEquals(1, publisher.postEvents.size());
        assertEquals("post.updated", publisher.postEvents.get(0).type());
    }

    @Test
    void deletePublishedEmitsEvent() {
        final InMemoryPostRepository repo = new InMemoryPostRepository();
        final RecordingFeedEventPublisher publisher = new RecordingFeedEventPublisher();
        final AdminPostsService service = new AdminPostsService(repo, publisher);

        final PostRepository.PostRecord published = new PostRepository.PostRecord(
                UUID.randomUUID(),
                "Title",
                Slug.of("published"),
                null,
                List.of(Tag.of("tech")),
                PostStatus.PUBLISHED,
                null,
                "[]",
                UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        repo.save(published);

        service.deleteById(published.id());

        assertEquals(1, publisher.postEvents.size());
        assertEquals("post.deleted", publisher.postEvents.get(0).type());
    }

    @Test
    void importPostsEmitsFeedUpdated() {
        final InMemoryPostRepository repo = new InMemoryPostRepository();
        final RecordingFeedEventPublisher publisher = new RecordingFeedEventPublisher();
        final AdminPostsService service = new AdminPostsService(repo, publisher);

        final ImportPostCommand item = new ImportPostCommand(
                "Title",
                "Excerpt",
                null,
                "[]",
                Instant.parse("2024-01-02T00:00:00Z")
        );

        service.importPosts(List.of(item), UUID.randomUUID());

        assertEquals(1, publisher.feedUpdatedCount);
    }

    @Test
    void resetAllEmitsFeedUpdated() {
        final InMemoryPostRepository repo = new InMemoryPostRepository();
        final RecordingFeedEventPublisher publisher = new RecordingFeedEventPublisher();
        final AdminPostsService service = new AdminPostsService(repo, publisher);

        service.resetAll();

        assertEquals(1, publisher.feedUpdatedCount);
    }

    private static final class InMemoryPostRepository implements PostRepository {
        private final Map<UUID, PostRecord> byId = new HashMap<>();
        private final Map<String, PostRecord> bySlug = new HashMap<>();

        @Override
        public Optional<PostRecord> findBySlug(final Slug slug) {
            return Optional.ofNullable(bySlug.get(slug.value()));
        }

        @Override
        public Optional<PostRecord> findById(final UUID id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<PostRecord> findAll(final int page, final int size) {
            return List.copyOf(byId.values());
        }

        @Override
        public List<PostRecord> findByStatus(final PostStatus status, final int page, final int size) {
            return byId.values().stream()
                    .filter(post -> post.status() == status)
                    .toList();
        }

        @Override
        public List<PostRecord> findPublished(final int page, final int size) {
            return byId.values().stream()
                    .filter(post -> post.status() == PostStatus.PUBLISHED)
                    .toList();
        }

        @Override
        public FeedStats fetchPublishedFeedStats() {
            final var published = byId.values().stream()
                    .filter(post -> post.status() == PostStatus.PUBLISHED)
                    .toList();
            final Instant lastUpdatedAt = published.stream()
                    .map(PostRecord::updatedAt)
                    .max(Instant::compareTo)
                    .orElse(null);
            return new FeedStats(lastUpdatedAt, published.size());
        }

        @Override
        public PostRecord save(final PostRecord post) {
            byId.put(post.id(), post);
            bySlug.put(post.slug().value(), post);
            return post;
        }

        @Override
        public void deleteById(final UUID id) {
            final PostRecord existing = byId.remove(id);
            if (existing != null) {
                bySlug.remove(existing.slug().value());
            }
        }

        @Override
        public void deleteAll() {
            byId.clear();
            bySlug.clear();
        }
    }

    private static final class NoOpFeedEventPublisher implements FeedEventPublisher {
        @Override
        public void publishPostEvent(final PostEvent event) {
        }

        @Override
        public void publishFeedUpdated(final Instant updatedAt) {
        }
    }

    private static final class RecordingFeedEventPublisher implements FeedEventPublisher {
        private final List<PostEvent> postEvents = new java.util.ArrayList<>();
        private int feedUpdatedCount = 0;

        @Override
        public void publishPostEvent(final PostEvent event) {
            postEvents.add(event);
        }

        @Override
        public void publishFeedUpdated(final Instant updatedAt) {
            feedUpdatedCount += 1;
        }
    }
}

