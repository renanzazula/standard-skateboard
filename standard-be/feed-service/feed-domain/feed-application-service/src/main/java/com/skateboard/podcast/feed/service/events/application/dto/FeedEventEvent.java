package com.skateboard.podcast.feed.service.events.application.dto;

import java.time.Instant;
import java.util.UUID;

public record FeedEventEvent(String type, UUID eventId, String slug, Instant updatedAt) {}

