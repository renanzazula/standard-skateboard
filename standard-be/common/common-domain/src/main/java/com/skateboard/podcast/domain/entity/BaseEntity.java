package com.skateboard.podcast.domain.entity;

import java.util.Objects;

public abstract class BaseEntity<ID> {
    private final ID id;

    protected BaseEntity(final ID id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public ID id() {
        return id;
    }
}
