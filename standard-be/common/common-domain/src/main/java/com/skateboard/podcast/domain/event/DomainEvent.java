package com.skateboard.podcast.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
