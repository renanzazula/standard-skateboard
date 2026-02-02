package com.skateboard.podcast.shared.domain.valueobject;

import com.skateboard.podcast.shared.domain.exception.ValidationException;

import java.net.URI;

public record Url(String value) {

    public Url {
        if (value == null || value.isBlank()) {
            throw new ValidationException("url cannot be blank");
        }
        final String trimmed = value.trim();
        final URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (final IllegalArgumentException e) {
            throw new ValidationException("invalid url: " + value);
        }
        final String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new ValidationException("url must be http/https: " + value);
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new ValidationException("url must have a host: " + value);
        }
        value = trimmed;
    }

    public static Url of(final String value) {
        return new Url(value);
    }
}
