package com.skateboard.podcast.domain.valueobject;

import com.skateboard.podcast.domain.exception.ValidationException;

import java.util.Locale;

public enum UserStatus {
    ACTIVE,
    DISABLED;

    public static UserStatus from(final String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("status cannot be blank");
        }
        try {
            return UserStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException e) {
            throw new ValidationException("unknown status: " + raw);
        }
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
