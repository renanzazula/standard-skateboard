package com.skateboard.podcast.feed.service.events.application.dto;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public record FeedEventsVersion(String etag, Instant lastUpdatedAt) {

    public long lastModifiedEpochMillis() {
        return lastUpdatedAt.toEpochMilli();
    }

    public boolean isNotModifiedSince(final String ifModifiedSince) {
        if (ifModifiedSince == null || ifModifiedSince.isBlank()) {
            return false;
        }
        try {
            final Instant since = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(ifModifiedSince));
            return !lastUpdatedAt.isAfter(since);
        } catch (final DateTimeParseException ignored) {
            return false;
        }
    }

    public boolean matchesEtag(final String ifNoneMatch) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank()) {
            return false;
        }
        final String[] parts = ifNoneMatch.split(",");
        for (final String part : parts) {
            final String candidate = normalizeEtag(part);
            if (etag.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    public static String buildEtag(final Instant lastUpdatedAt, final long totalCount, final int page, final int size) {
        return lastUpdatedAt.toEpochMilli() + ":" + totalCount + ":" + page + ":" + size;
    }

    public static String formatHttpDate(final Instant instant) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC).format(instant);
    }

    private static String normalizeEtag(final String raw) {
        if (raw == null) {
            return "";
        }
        String candidate = raw.trim();
        if (candidate.startsWith("W/")) {
            candidate = candidate.substring(2).trim();
        }
        if (candidate.startsWith("\"") && candidate.endsWith("\"") && candidate.length() > 1) {
            candidate = candidate.substring(1, candidate.length() - 1);
        }
        return candidate;
    }
}

