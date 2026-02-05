package com.skateboard.podcast.feed.service.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostSummaryView(
        UUID id,
        String title,
        String slug,
        String excerpt,
        List<String> tags,
        String status,
        String thumbnailJson,
        Instant publishedAt
) {}
