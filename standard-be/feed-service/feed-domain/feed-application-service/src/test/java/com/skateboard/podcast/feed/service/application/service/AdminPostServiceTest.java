package com.skateboard.podcast.feed.service.application.service;

import com.skateboard.podcast.domain.exception.NotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminPostServiceTest {

    @Test
    void createDraftRequiresTitleAndContent() {
        final AdminPostService service = new AdminPostService(new InMemoryPostRepository());

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
        final AdminPostService service = new AdminPostService(new InMemoryPostRepository());

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
        final AdminPostService service = new AdminPostService(repo);

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
        final AdminPostService service = new AdminPostService(repo);

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
        public List<PostRecord> findPublished(final int page, final int size) {
            return byId.values().stream()
                    .filter(post -> post.status() == PostStatus.PUBLISHED)
                    .toList();
        }

        @Override
        public PostRecord save(final PostRecord post) {
            byId.put(post.id(), post);
            bySlug.put(post.slug().value(), post);
            return post;
        }
    }
}
