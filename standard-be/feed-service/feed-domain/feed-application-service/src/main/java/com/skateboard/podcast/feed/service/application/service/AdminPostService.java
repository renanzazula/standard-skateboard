package com.skateboard.podcast.feed.service.application.service;

import com.skateboard.podcast.feed.service.application.dto.PostDetailsView;
import com.skateboard.podcast.feed.service.application.port.in.AdminPostUseCase;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import com.skateboard.podcast.domain.exception.NotFoundException;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.PostStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AdminPostService implements AdminPostUseCase {

    private final PostRepository postRepository;

    public AdminPostService(final PostRepository postRepository) {
        this.postRepository = postRepository;
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

    private static List<Tag> toTagValues(final List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(Tag::of)
                .toList();
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
}
