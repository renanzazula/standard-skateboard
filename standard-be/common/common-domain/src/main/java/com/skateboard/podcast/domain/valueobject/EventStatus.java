package com.skateboard.podcast.domain.valueobject;

import com.skateboard.podcast.domain.exception.ValidationException;

import java.util.Locale;

public enum EventStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED;

    public static EventStatus from(final String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("status cannot be blank");
        }
        try {
            return EventStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException e) {
            throw new ValidationException("unknown status: " + raw);
        }
    }
}
