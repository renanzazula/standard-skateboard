package com.skateboard.podcast.domain.valueobject;

import com.skateboard.podcast.domain.exception.ValidationException;

import java.util.Locale;

public enum Provider {
    MANUAL,
    GOOGLE,
    APPLE,
    FACEBOOK,
    PASSCODE;

    public static Provider from(final String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("provider cannot be blank");
        }
        try {
            return Provider.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException e) {
            throw new ValidationException("unknown provider: " + raw);
        }
    }
}
