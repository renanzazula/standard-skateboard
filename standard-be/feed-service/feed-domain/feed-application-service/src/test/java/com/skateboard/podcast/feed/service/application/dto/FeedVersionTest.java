package com.skateboard.podcast.feed.service.application.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedVersionTest {

    @Test
    void matchesEtagSupportsQuotesWeakAndLists() {
        final FeedVersion version = new FeedVersion("abc-123", Instant.parse("2024-01-01T00:00:00Z"));

        assertTrue(version.matchesEtag("\"abc-123\""));
        assertTrue(version.matchesEtag("W/\"abc-123\""));
        assertTrue(version.matchesEtag("foo, \"abc-123\", bar"));
        assertFalse(version.matchesEtag("\"nope\""));
    }

    @Test
    void isNotModifiedSinceRejectsInvalidDate() {
        final FeedVersion version = new FeedVersion("etag", Instant.parse("2024-01-01T00:00:00Z"));
        assertFalse(version.isNotModifiedSince("not-a-date"));
    }

    @Test
    void isNotModifiedSinceReturnsTrueWhenUnchanged() {
        final Instant updatedAt = Instant.parse("2024-01-02T00:00:00Z");
        final FeedVersion version = new FeedVersion("etag", updatedAt);
        final String since = DateTimeFormatter.RFC_1123_DATE_TIME
                .withZone(ZoneOffset.UTC)
                .format(updatedAt);
        assertTrue(version.isNotModifiedSince(since));
    }
}
