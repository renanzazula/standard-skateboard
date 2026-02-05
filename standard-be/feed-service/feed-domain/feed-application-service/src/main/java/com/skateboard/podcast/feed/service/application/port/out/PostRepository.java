package com.skateboard.podcast.feed.service.application.port.out;

import com.skateboard.podcast.domain.valueobject.PostStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {

    Optional<PostRecord> findBySlug(Slug slug);

    Optional<PostRecord> findById(UUID id);

    List<PostRecord> findPublished(int page, int size);

    PostRecord save(PostRecord post);

    record PostRecord(
            UUID id,
            String title,
            Slug slug,
            String excerpt,
            List<Tag> tags,
            PostStatus status,
            String thumbnailJson,
            String contentJson,
            UUID authorId,
            Instant createdAt,
            Instant updatedAt,
            Instant publishedAt
    ) {}
}
