package com.skateboard.podcast.domain.valueobject;


import com.skateboard.podcast.domain.exception.ValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    // Simple pragmatic email validation (good enough for most backends)
    private static final Pattern EMAIL =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new ValidationException("email cannot be blank");
        }
        final String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(normalized).matches()) {
            throw new ValidationException("invalid email: " + value);
        }
        value = normalized;
    }

    public static Email of(final String value) {
        return new Email(value);
    }
}
