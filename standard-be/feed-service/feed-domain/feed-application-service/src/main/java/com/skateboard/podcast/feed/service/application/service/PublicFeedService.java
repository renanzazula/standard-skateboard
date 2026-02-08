package com.skateboard.podcast.feed.service.application.service;

import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.Tag;
import com.skateboard.podcast.feed.service.application.dto.FeedItemSummaryView;
import com.skateboard.podcast.feed.service.application.dto.FeedVersion;
import com.skateboard.podcast.feed.service.application.port.in.PublicFeedUseCase;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import com.skateboard.podcast.feed.service.events.application.port.out.EventRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PublicFeedService implements PublicFeedUseCase {

    private static final String TYPE_POST = "POST";
    private static final String TYPE_EVENT = "EVENT";

    private final PostRepository postRepository;
    private final EventRepository eventRepository;

    public PublicFeedService(
            final PostRepository postRepository,
            final EventRepository eventRepository
    ) {
        this.postRepository = postRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public List<FeedItemSummaryView> listPublished(final int page, final int size) {
        validatePageAndSize(page, size);

        final int fetchSize = (page + 1) * size;
        final List<PostRepository.PostRecord> posts = postRepository.findPublished(0, fetchSize);
        final List<EventRepository.EventRecord> events = eventRepository.findPublished(0, fetchSize);

        final List<FeedItemSummaryView> items = new ArrayList<>(posts.size() + events.size());
        for (final PostRepository.PostRecord post : posts) {
            items.add(toPostItem(post));
        }
        for (final EventRepository.EventRecord event : events) {
            items.add(toEventItem(event));
        }

        items.sort(Comparator.comparing(PublicFeedService::resolveSortAt).reversed());

        final int from = page * size;
        if (from >= items.size()) {
            return List.of();
        }
        final int to = Math.min(from + size, items.size());
        return items.subList(from, to);
    }

    @Override
    public FeedVersion getFeedVersion(final int page, final int size) {
        validatePageAndSize(page, size);

        final PostRepository.FeedStats postStats = postRepository.fetchPublishedFeedStats();
        final EventRepository.EventStats eventStats = eventRepository.fetchPublishedStats();

        final Instant postUpdatedAt = postStats.lastUpdatedAt() == null
                ? Instant.EPOCH
                : postStats.lastUpdatedAt();
        final Instant eventUpdatedAt = eventStats.lastUpdatedAt() == null
                ? Instant.EPOCH
                : eventStats.lastUpdatedAt();
        final Instant lastUpdatedAt = postUpdatedAt.isAfter(eventUpdatedAt)
                ? postUpdatedAt
                : eventUpdatedAt;
        final long totalCount = postStats.totalCount() + eventStats.totalCount();
        final String etag = FeedVersion.buildEtag(lastUpdatedAt, totalCount, page, size);
        return new FeedVersion(etag, lastUpdatedAt);
    }

    private static void validatePageAndSize(final int page, final int size) {
        if (page < 0) throw new ValidationException("page must be >= 0");
        if (size < 1 || size > 50) throw new ValidationException("size must be between 1 and 50");
    }

    private static FeedItemSummaryView toPostItem(final PostRepository.PostRecord post) {
        return new FeedItemSummaryView(
                TYPE_POST,
                post.id(),
                post.title(),
                post.slug().value(),
                post.excerpt(),
                toTagStrings(post.tags()),
                post.status().name(),
                post.thumbnailJson(),
                post.publishedAt(),
                null,
                null,
                null,
                null,
                null,
                post.createdAt(),
                post.updatedAt(),
                post.authorId()
        );
    }

    private static FeedItemSummaryView toEventItem(final EventRepository.EventRecord event) {
        return new FeedItemSummaryView(
                TYPE_EVENT,
                event.id(),
                event.title(),
                event.slug().value(),
                event.excerpt(),
                toTagStrings(event.tags()),
                event.status().name(),
                event.thumbnailJson(),
                null,
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

    private static Instant resolveSortAt(final FeedItemSummaryView item) {
        if (item == null) {
            return Instant.EPOCH;
        }
        if (TYPE_EVENT.equalsIgnoreCase(item.type())) {
            return firstNonNull(item.startAt(), item.updatedAt(), item.createdAt());
        }
        return firstNonNull(item.publishedAt(), item.updatedAt(), item.createdAt());
    }

    private static Instant firstNonNull(final Instant primary, final Instant secondary, final Instant fallback) {
        if (primary != null) {
            return primary;
        }
        if (secondary != null) {
            return secondary;
        }
        if (fallback != null) {
            return fallback;
        }
        return Instant.EPOCH;
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
