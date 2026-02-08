package com.skateboard.podcast.feed.service.events.application.adapter.in.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.feed.service.events.application.dto.EventDetailsView;
import com.skateboard.podcast.feed.service.events.application.dto.EventSummaryView;
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
import java.util.Locale;

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
            final List<JsonNode> normalized = safe.stream()
                    .filter(block -> block != null)
                    .map(this::normalizeBlock)
                    .toList();
            return objectMapper.writeValueAsString(normalized);
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

    private List<Block> readContent(final String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, BLOCK_LIST);
        } catch (final JsonProcessingException e) {
            final String unwrapped = unwrapJsonString(json);
            if (unwrapped != null) {
                try {
                    return objectMapper.readValue(unwrapped, BLOCK_LIST);
                } catch (final JsonProcessingException ignored) {
                    // fall through
                }
            }
            throw new ValidationException("invalid stored content");
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

    private JsonNode normalizeBlock(final Block block) {
        final JsonNode node = objectMapper.valueToTree(block);
        if (node instanceof final ObjectNode objectNode) {
            if (!objectNode.has("type")) {
                final String typeValue = block.getType() != null
                        ? block.getType().getValue()
                        : inferType(block);
                if (typeValue != null && !typeValue.isBlank()) {
                    objectNode.put("type", typeValue);
                }
            }
        }
        return node;
    }

    private static String inferType(final Block block) {
        if (block == null) {
            return null;
        }
        String name = block.getClass().getSimpleName();
        if (name.endsWith("Block")) {
            name = name.substring(0, name.length() - "Block".length());
        }
        return name.isEmpty() ? null : name.toLowerCase(Locale.ROOT);
    }
}

