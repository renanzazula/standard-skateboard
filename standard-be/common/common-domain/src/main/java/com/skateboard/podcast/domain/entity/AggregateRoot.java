package com.skateboard.podcast.domain.entity;

public abstract class AggregateRoot<ID> extends BaseEntity<ID> {
    protected AggregateRoot(final ID id) {
        super(id);
    }
}
