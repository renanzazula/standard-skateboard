package com.skateboard.podcast.domain.valueobject;


import com.skateboard.podcast.domain.exception.ValidationException;

import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        if (value == null) throw new ValidationException("userId cannot be null");
    }

    public static UserId of(final UUID value) {
        return new UserId(value);
    }

    public static UserId fromString(final String raw) {
        if (raw == null || raw.isBlank()) throw new ValidationException("userId cannot be blank");
        try {
            return new UserId(UUID.fromString(raw));
        } catch (final IllegalArgumentException e) {
            throw new ValidationException("userId is not a valid UUID: " + raw);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
