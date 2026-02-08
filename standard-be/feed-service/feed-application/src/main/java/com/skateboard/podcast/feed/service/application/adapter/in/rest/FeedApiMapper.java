package com.skateboard.podcast.feed.service.application.adapter.in.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.feed.service.application.dto.FeedItemSummaryView;
import com.skateboard.podcast.standardbe.api.model.FeedItemStatus;
import com.skateboard.podcast.standardbe.api.model.FeedItemSummary;
import com.skateboard.podcast.standardbe.api.model.FeedItemType;
import com.skateboard.podcast.standardbe.api.model.ImageRef;
import com.skateboard.podcast.standardbe.api.model.PageFeedItemSummary;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class FeedApiMapper {

    private final ObjectMapper objectMapper;

    public FeedApiMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PageFeedItemSummary toPageFeedItemSummary(
            final List<FeedItemSummaryView> items,
            final int page,
            final int size
    ) {
        final List<FeedItemSummary> summaries = items == null
                ? List.of()
                : items.stream().map(this::toFeedItemSummary).toList();
        return new PageFeedItemSummary()
                .items(summaries)
                .page(page)
                .size(size)
                .totalItems(summaries.size());
    }

    public FeedItemSummary toFeedItemSummary(final FeedItemSummaryView view) {
        final ImageRef thumbnail = readThumbnail(view.thumbnailJson());
        return new FeedItemSummary()
                .type(FeedItemType.fromValue(view.type()))
                .id(view.id())
                .title(view.title())
                .slug(view.slug())
                .excerpt(view.excerpt())
                .tags(view.tags())
                .status(FeedItemStatus.fromValue(view.status()))
                .thumbnail(thumbnail)
                .publishedAt(toOffsetDateTime(view.publishedAt()))
                .startAt(toOffsetDateTime(view.startAt()))
                .endAt(toOffsetDateTime(view.endAt()))
                .timezone(view.timezone())
                .location(view.location())
                .ticketsUrl(view.ticketsUrl())
                .createdAt(toOffsetDateTime(view.createdAt()))
                .updatedAt(toOffsetDateTime(view.updatedAt()))
                .createdBy(view.createdBy());
    }

    private ImageRef readThumbnail(final String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ImageRef.class);
        } catch (final JsonProcessingException e) {
            final String unwrapped = unwrapJsonString(json);
            if (unwrapped != null) {
                try {
                    return objectMapper.readValue(unwrapped, ImageRef.class);
                } catch (final JsonProcessingException ignored) {
                    // fall through
                }
            }
            throw new ValidationException("invalid stored thumbnail");
        }
    }

    private String unwrapJsonString(final String json) {
        try {
            final JsonNode node = objectMapper.readTree(json);
            if (node != null && node.isTextual()) {
                return node.asText();
            }
        } catch (final JsonProcessingException ignored) {
            // ignore and return null
        }
        return null;
    }

    private static OffsetDateTime toOffsetDateTime(final java.time.Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
