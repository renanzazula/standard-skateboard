package com.skateboard.podcast.event.service.application.dto;

import java.time.Instant;
import java.util.UUID;

public record EventEvent(String type, UUID eventId, String slug, Instant updatedAt) {}
