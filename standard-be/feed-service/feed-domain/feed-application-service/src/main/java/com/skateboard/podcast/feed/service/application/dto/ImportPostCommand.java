package com.skateboard.podcast.feed.service.application.dto;

import java.time.Instant;

public record ImportPostCommand(
        String title,
        String excerpt,
        String thumbnailJson,
        String contentJson,
        Instant publishedAt
) {}
