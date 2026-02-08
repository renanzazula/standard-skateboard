package com.skateboard.podcast.feed.service.application.service;

import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.PostStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicPostsServiceTest {

    @Test
    void listPublishedValidatesPageAndSize() {
        final PublicPostsService service = new PublicPostsService(new InMemoryPostRepository());

        assertThrows(ValidationException.class, () -> service.listPublished(-1, 10));
        assertThrows(ValidationException.class, () -> service.listPublished(0, 0));
        assertThrows(ValidationException.class, () -> service.listPublished(0, 51));
    }

    @Test
    void getBySlugReturnsOnlyPublishedPosts() {
        final InMemoryPostRepository repo = new InMemoryPostRepository();
        final PublicPostsService service = new PublicPostsService(repo);

        repo.save(postRecord("draft-post", PostStatus.DRAFT));
        repo.save(postRecord("published-post", PostStatus.PUBLISHED));

        assertFalse(service.getBySlug("draft-post").isPresent());
        assertTrue(service.getBySlug("published-post").isPresent());
        assertEquals("published-post", service.getBySlug("published-post").orElseThrow().slug());
    }

    private static PostRepository.PostRecord postRecord(final String slug, final PostStatus status) {
        return new PostRepository.PostRecord(
                UUID.randomUUID(),
                "Title",
                Slug.of(slug),
                "Excerpt",
                List.of(Tag.of("tech")),
                status,
                null,
                "[]",
                UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                status == PostStatus.PUBLISHED ? Instant.parse("2024-01-02T00:00:00Z") : null
        );
    }

    private static final class InMemoryPostRepository implements PostRepository {
        private final Map<String, PostRecord> bySlug = new HashMap<>();
        private final Map<UUID, PostRecord> byId = new HashMap<>();

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
            return bySlug.values().stream()
                    .filter(post -> post.status() == PostStatus.PUBLISHED)
                    .toList();
        }

        @Override
        public FeedStats fetchPublishedFeedStats() {
            final var published = bySlug.values().stream()
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
            bySlug.put(post.slug().value(), post);
            byId.put(post.id(), post);
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
}

