package com.skateboard.podcast.domain.valueobject;

import com.skateboard.podcast.domain.exception.ValidationException;

import java.util.Locale;

public enum PostStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED;

    public static PostStatus from(final String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("status cannot be blank");
        }
        try {
            return PostStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException e) {
            throw new ValidationException("unknown status: " + raw);
        }
    }
}
