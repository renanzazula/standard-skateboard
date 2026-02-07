package com.skateboard.podcast.feed.service.application.service;

import com.skateboard.podcast.feed.service.application.dto.PostDetailsView;
import com.skateboard.podcast.feed.service.application.dto.PostSummaryView;
import com.skateboard.podcast.feed.service.application.dto.FeedVersion;
import com.skateboard.podcast.feed.service.application.port.in.PublicFeedUseCase;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.PostStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class PublicFeedService implements PublicFeedUseCase {

    private final PostRepository postRepository;

    public PublicFeedService(final PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<PostSummaryView> listPublished(final int page, final int size) {
        if (page < 0) throw new ValidationException("page must be >= 0");
        if (size < 1 || size > 50) throw new ValidationException("size must be between 1 and 50");

        return postRepository.findPublished(page, size).stream()
                .map(PublicFeedService::toSummary)
                .toList();
    }

    @Override
    public Optional<PostDetailsView> getBySlug(final String slug) {
        final Slug slugValue = Slug.of(slug);
        return postRepository.findBySlug(slugValue)
                .filter(post -> post.status() == PostStatus.PUBLISHED)
                .map(PublicFeedService::toDetails);
    }

    @Override
    public FeedVersion getFeedVersion(final int page, final int size) {
        if (page < 0) throw new ValidationException("page must be >= 0");
        if (size < 1 || size > 50) throw new ValidationException("size must be between 1 and 50");

        final PostRepository.FeedStats stats = postRepository.fetchPublishedFeedStats();
        final Instant lastUpdatedAt = stats.lastUpdatedAt() == null
                ? Instant.EPOCH
                : stats.lastUpdatedAt();
        final String etag = FeedVersion.buildEtag(lastUpdatedAt, stats.totalCount(), page, size);
        return new FeedVersion(etag, lastUpdatedAt);
    }

    private static PostSummaryView toSummary(final PostRepository.PostRecord post) {
        return new PostSummaryView(
                post.id(),
                post.title(),
                post.slug().value(),
                post.excerpt(),
                toTagStrings(post.tags()),
                post.status().name(),
                post.thumbnailJson(),
                post.publishedAt()
        );
    }

    private static PostDetailsView toDetails(final PostRepository.PostRecord post) {
        return new PostDetailsView(
                post.id(),
                post.title(),
                post.slug().value(),
                post.excerpt(),
                toTagStrings(post.tags()),
                post.status().name(),
                post.thumbnailJson(),
                post.contentJson(),
                post.authorId(),
                post.publishedAt()
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
