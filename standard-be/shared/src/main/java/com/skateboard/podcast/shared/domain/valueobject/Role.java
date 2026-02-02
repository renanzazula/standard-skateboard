package com.skateboard.podcast.shared.domain.valueobject;

import com.skateboard.podcast.shared.domain.exception.ValidationException;

import java.util.Locale;

public enum Role {
        GUEST,
        USER,
        ADMIN;

        public static Role from(final String raw) {
                if (raw == null || raw.isBlank()) {
                        throw new ValidationException("role cannot be blank");
                }
                try {
                        return Role.valueOf(raw.trim().toUpperCase(Locale.ROOT));
                } catch (final IllegalArgumentException e) {
                        throw new ValidationException("unknown role: " + raw);
                }
        }
}
