package com.skateboard.podcast.domain.valueobject;

import com.skateboard.podcast.domain.exception.ValidationException;

public record Tag(String value) {

    public Tag {
        if (value == null || value.isBlank()) {
            throw new ValidationException("tag cannot be blank");
        }
        value = value.trim();
        if (value.isEmpty()) {
            throw new ValidationException("tag cannot be blank");
        }
    }

    public static Tag of(final String value) {
        return new Tag(value);
    }
}
