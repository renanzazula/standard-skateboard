package com.skateboard.podcast.event.service.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventSummaryView(
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
