package com.skateboard.podcast.event.service.application.adapter.in.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.event.service.application.dto.EventDetailsView;
import com.skateboard.podcast.event.service.application.dto.EventSummaryView;
import com.skateboard.podcast.standardbe.api.model.Block;
import com.skateboard.podcast.standardbe.api.model.EventDetails;
import com.skateboard.podcast.standardbe.api.model.EventStatus;
import com.skateboard.podcast.standardbe.api.model.EventSummary;
import com.skateboard.podcast.standardbe.api.model.ImageRef;
import com.skateboard.podcast.standardbe.api.model.PageEventSummary;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

@Component
public class EventApiMapper {

    private static final TypeReference<List<Block>> BLOCK_LIST = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public EventApiMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PageEventSummary toPageEventSummary(final List<EventSummaryView> items, final int page, final int size) {
        final List<EventSummary> summaries = items == null
                ? List.of()
                : items.stream().map(this::toEventSummary).toList();
        return new PageEventSummary()
                .items(summaries)
                .page(page)
                .size(size)
                .totalItems(summaries.size());
    }

    public EventSummary toEventSummary(final EventSummaryView view) {
        final ImageRef thumbnail = readThumbnail(view.thumbnailJson());
        return new EventSummary()
                .id(view.id())
                .title(view.title())
                .slug(view.slug())
                .excerpt(view.excerpt())
                .tags(view.tags())
                .status(EventStatus.fromValue(view.status()))
                .thumbnail(thumbnail)
                .startAt(toOffsetDateTime(view.startAt()))
                .endAt(toOffsetDateTime(view.endAt()))
                .timezone(view.timezone())
                .location(view.location())
                .ticketsUrl(view.ticketsUrl())
                .createdAt(toOffsetDateTime(view.createdAt()))
                .updatedAt(toOffsetDateTime(view.updatedAt()))
                .createdBy(view.createdBy());
    }

    public EventDetails toEventDetails(final EventDetailsView view) {
        final ImageRef thumbnail = readThumbnail(view.thumbnailJson());
        final List<Block> content = readContent(view.contentJson());
        return new EventDetails()
                .id(view.id())
                .title(view.title())
                .slug(view.slug())
                .excerpt(view.excerpt())
                .tags(view.tags())
                .status(EventStatus.fromValue(view.status()))
                .thumbnail(thumbnail)
                .content(content)
                .startAt(toOffsetDateTime(view.startAt()))
                .endAt(toOffsetDateTime(view.endAt()))
                .timezone(view.timezone())
                .location(view.location())
                .ticketsUrl(view.ticketsUrl())
                .createdAt(toOffsetDateTime(view.createdAt()))
                .updatedAt(toOffsetDateTime(view.updatedAt()))
                .createdBy(view.createdBy());
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
