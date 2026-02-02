package com.skateboard.podcast.shared.application.clock;

import java.time.Instant;

public interface Clock {
    Instant now();
}
