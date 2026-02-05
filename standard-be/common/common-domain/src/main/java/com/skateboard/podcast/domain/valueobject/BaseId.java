package com.skateboard.podcast.domain.valueobject;

import java.util.Objects;

public abstract class BaseId<T> {
    private final T value;

    protected BaseId(final T value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public T value() {
        return value;
    }
}
