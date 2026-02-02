package com.skateboard.podcast.shared.domain.valueobject;

import com.skateboard.podcast.shared.domain.exception.ValidationException;

import java.util.Locale;

public record Slug(String value) {

    public Slug {
        if (value == null || value.isBlank()) {
            throw new ValidationException("slug cannot be blank");
        }
        final String normalized = normalize(value);
        if (normalized.length() > 200) {
            throw new ValidationException("slug too long (max 200): " + normalized.length());
        }
        value = normalized;
    }

    public static Slug of(final String value) {
        return new Slug(value);
    }

    /**
     * Simple slug normalization:
     * - lowercase
     * - spaces/underscores => dash
     * - remove invalid chars
     * - collapse multiple dashes
     */
    public static String normalize(final String raw) {
        String s = raw.trim().toLowerCase(Locale.ROOT);
        s = s.replace('_', '-').replace(' ', '-');
        s = s.replaceAll("[^a-z0-9-]", "");
        s = s.replaceAll("-{2,}", "-");
        s = s.replaceAll("^-|-$", "");
        if (s.isBlank()) {
            throw new ValidationException("slug normalizes to empty value: " + raw);
        }
        return s;
    }
}
