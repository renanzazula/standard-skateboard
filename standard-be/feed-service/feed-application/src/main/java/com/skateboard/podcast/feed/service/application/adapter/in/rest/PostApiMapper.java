package com.skateboard.podcast.feed.service.application.adapter.in.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.feed.service.application.dto.PostDetailsView;
import com.skateboard.podcast.feed.service.application.dto.PostSummaryView;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.standardbe.api.model.Block;
import com.skateboard.podcast.standardbe.api.model.ImageRef;
import com.skateboard.podcast.standardbe.api.model.PagePostSummary;
import com.skateboard.podcast.standardbe.api.model.PostDetails;
import com.skateboard.podcast.standardbe.api.model.PostStatus;
import com.skateboard.podcast.standardbe.api.model.PostSummary;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

@Component
public class PostApiMapper {

    private static final TypeReference<List<Block>> BLOCK_LIST = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public PostApiMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PagePostSummary toPagePostSummary(final List<PostSummaryView> items, final int page, final int size) {
        final List<PostSummary> summaries = items == null
                ? List.of()
                : items.stream().map(this::toPostSummary).toList();
        return new PagePostSummary()
                .items(summaries)
                .page(page)
                .size(size)
                .totalItems(summaries.size());
    }

    public PostSummary toPostSummary(final PostSummaryView view) {
        final ImageRef thumbnail = readThumbnail(view.thumbnailJson());
        return new PostSummary()
                .id(view.id())
                .title(view.title())
                .slug(view.slug())
                .excerpt(view.excerpt())
                .tags(view.tags())
                .status(PostStatus.fromValue(view.status()))
                .thumbnail(thumbnail)
                .publishedAt(toOffsetDateTime(view.publishedAt()));
    }

    public PostDetails toPostDetails(final PostDetailsView view) {
        final ImageRef thumbnail = readThumbnail(view.thumbnailJson());
        final List<Block> content = readContent(view.contentJson());
        return new PostDetails()
                .id(view.id())
                .title(view.title())
                .slug(view.slug())
                .excerpt(view.excerpt())
                .tags(view.tags())
                .status(PostStatus.fromValue(view.status()))
                .thumbnail(thumbnail)
                .content(content)
                .publishedAt(toOffsetDateTime(view.publishedAt()));
    }

    public String writeThumbnailJson(final ImageRef imageRef) {
        if (imageRef == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(imageRef);
        } catch (final JsonProcessingException e) {
            throw new ValidationException("invalid thumbnail payload");
        }
    }

    public String writeContentJson(final List<Block> blocks) {
        final List<Block> safe = blocks == null ? List.of() : blocks;
        try {
            return objectMapper.writeValueAsString(safe);
        } catch (final JsonProcessingException e) {
            throw new ValidationException("invalid content payload");
        }
    }

    private ImageRef readThumbnail(final String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ImageRef.class);
        } catch (final JsonProcessingException e) {
            throw new ValidationException("invalid stored thumbnail");
        }
    }

    private List<Block> readContent(final String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, BLOCK_LIST);
        } catch (final JsonProcessingException e) {
            throw new ValidationException("invalid stored content");
        }
    }

    private static OffsetDateTime toOffsetDateTime(final java.time.Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
