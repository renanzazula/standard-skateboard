package com.skateboard.podcast.shared.application.clock;

import java.time.Instant;

public class SystemClock implements Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
