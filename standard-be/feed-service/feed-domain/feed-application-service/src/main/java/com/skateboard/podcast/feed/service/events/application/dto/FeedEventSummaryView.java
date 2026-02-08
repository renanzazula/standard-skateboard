package com.skateboard.podcast.feed.service.events.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FeedEventSummaryView(
        UUID id,
        String title,
        String slug,
        String excerpt,
        List<String> tags,
        String status,
        String thumbnailJson,
        Instant startAt,
        Instant endAt,
        String timezone,
        String location,
        String ticketsUrl,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy
) {}

