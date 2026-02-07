package com.skateboard.podcast.feed.service.application.service;

import com.skateboard.podcast.feed.service.application.dto.ImportPostCommand;
import com.skateboard.podcast.feed.service.application.dto.PostDetailsView;
import com.skateboard.podcast.feed.service.application.dto.PostSummaryView;
import com.skateboard.podcast.feed.service.application.port.in.AdminPostUseCase;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import com.skateboard.podcast.domain.exception.NotFoundException;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.PostStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class AdminPostService implements AdminPostUseCase {

    private final PostRepository postRepository;

    public AdminPostService(final PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<PostSummaryView> list(final int page, final int size, final PostStatus status) {
        final List<PostRepository.PostRecord> records = status == null
                ? postRepository.findAll(page, size)
                : postRepository.findByStatus(status, page, size);
        return records.stream()
                .map(AdminPostService::toSummary)
                .toList();
    }

    @Override
    public Optional<PostDetailsView> getById(final UUID id) {
        return postRepository.findById(id).map(AdminPostService::toDetails);
    }

    @Override
    public PostDetailsView createDraft(
            final String title,
            final String slug,
            final String excerpt,
            final List<String> tags,
            final String thumbnailJson,
            final String contentJson,
            final UUID authorId
    ) {
        if (title == null || title.isBlank()) throw new ValidationException("title cannot be blank");
        if (contentJson == null || contentJson.isBlank()) throw new ValidationException("content cannot be blank");

        final Slug slugValue = Slug.of(slug);
        final List<Tag> tagValues = toTagValues(tags);
        final Instant now = Instant.now();
        final UUID postId = UUID.randomUUID();

        final var record = new PostRepository.PostRecord(
                postId,
                title.trim(),
                slugValue,
                excerpt,
                tagValues,
                PostStatus.DRAFT,
                thumbnailJson,
                contentJson,
                authorId,
                now,
                now,
                null
        );
        final var saved = postRepository.save(record);
        return toDetails(saved);
    }

    @Override
    public PostDetailsView updateById(
            final UUID id,
            final String title,
            final String slug,
            final String excerpt,
            final List<String> tags,
            final String thumbnailJson,
            final String contentJson
    ) {
        if (title == null || title.isBlank()) throw new ValidationException("title cannot be blank");
        if (contentJson == null || contentJson.isBlank()) throw new ValidationException("content cannot be blank");

        final var existing = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("post not found"));

        final Slug slugValue = Slug.of(slug);
        final List<Tag> tagValues = toTagValues(tags);
        final Instant now = Instant.now();

        final var updated = new PostRepository.PostRecord(
                existing.id(),
                title.trim(),
                slugValue,
                excerpt,
                tagValues,
                existing.status(),
                thumbnailJson,
                contentJson,
                existing.authorId(),
                existing.createdAt(),
                now,
                existing.publishedAt()
        );
        final var saved = postRepository.save(updated);
        return toDetails(saved);
    }

    @Override
    public PostDetailsView publishById(final UUID id) {
        final var existing = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("post not found"));

        final Instant now = Instant.now();
        final Instant publishedAt = existing.publishedAt() == null ? now : existing.publishedAt();
        final var updated = new PostRepository.PostRecord(
                existing.id(),
                existing.title(),
                existing.slug(),
                existing.excerpt(),
                existing.tags(),
                PostStatus.PUBLISHED,
                existing.thumbnailJson(),
                existing.contentJson(),
                existing.authorId(),
                existing.createdAt(),
                now,
                publishedAt
        );
        final var saved = postRepository.save(updated);
        return toDetails(saved);
    }

    @Override
    public void deleteById(final UUID id) {
        if (postRepository.findById(id).isEmpty()) {
            throw new NotFoundException("post not found");
        }
        postRepository.deleteById(id);
    }

    @Override
    public List<PostSummaryView> importPosts(final List<ImportPostCommand> items, final UUID authorId) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        final Set<String> seenSlugs = new HashSet<>();
        return items.stream()
                .map(item -> importOne(item, authorId, seenSlugs))
                .toList();
    }

    @Override
    public void resetAll() {
        postRepository.deleteAll();
    }

    private static List<Tag> toTagValues(final List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(Tag::of)
                .toList();
    }

    private PostSummaryView importOne(
            final ImportPostCommand item,
            final UUID authorId,
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

        final String slugValue = uniqueSlug(item.title(), seenSlugs);
        final Instant now = Instant.now();
        final Instant publishedAt = item.publishedAt() == null ? now : item.publishedAt();

        final var record = new PostRepository.PostRecord(
                UUID.randomUUID(),
                item.title().trim(),
                Slug.of(slugValue),
                item.excerpt(),
                List.of(),
                PostStatus.PUBLISHED,
                item.thumbnailJson(),
                item.contentJson(),
                authorId,
                now,
                now,
                publishedAt
        );
        final var saved = postRepository.save(record);
        return toSummary(saved);
    }

    private String uniqueSlug(final String title, final Set<String> seenSlugs) {
        final String base = Slug.normalize(title);
        String candidate = base;
        int counter = 1;
        while (seenSlugs.contains(candidate)
                || postRepository.findBySlug(Slug.of(candidate)).isPresent()) {
            candidate = base + "-" + counter;
            counter += 1;
        }
        seenSlugs.add(candidate);
        return candidate;
    }

    private static PostDetailsView toDetails(final PostRepository.PostRecord post) {
        return new PostDetailsView(
                post.id(),
                post.title(),
                post.slug().value(),
                post.excerpt(),
                post.tags().stream().map(Tag::value).toList(),
                post.status().name(),
                post.thumbnailJson(),
                post.contentJson(),
                post.authorId(),
                post.publishedAt()
        );
    }

    private static PostSummaryView toSummary(final PostRepository.PostRecord post) {
        return new PostSummaryView(
                post.id(),
                post.title(),
                post.slug().value(),
                post.excerpt(),
                post.tags().stream().map(Tag::value).toList(),
                post.status().name(),
                post.thumbnailJson(),
                post.publishedAt()
        );
    }
}
