package com.skateboard.podcast.feed.service.events.application.dto;

import java.time.Instant;
import java.util.List;

public record FeedEventImportCommand(
        String title,
        String slug,
        String excerpt,
        List<String> tags,
        String thumbnailJson,
        String contentJson,
        Instant startAt,
        Instant endAt,
        String timezone,
        String location,
        String ticketsUrl
) {}

