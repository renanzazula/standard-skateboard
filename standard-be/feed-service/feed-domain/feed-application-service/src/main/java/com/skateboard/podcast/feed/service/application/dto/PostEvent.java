package com.skateboard.podcast.feed.service.application.dto;

import java.time.Instant;
import java.util.UUID;

public record PostEvent(String type, UUID postId, String slug, Instant updatedAt) {}
