package com.skateboard.podcast.feed.service.events.application.adapter.in.rest;

import com.skateboard.podcast.feed.service.events.application.port.in.PublicFeedEventsUseCase;
import com.skateboard.podcast.standardbe.api.PublicEventsApi;
import com.skateboard.podcast.standardbe.api.model.EventDetails;
import com.skateboard.podcast.standardbe.api.model.PageEventSummary;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
public class PublicFeedEventsController implements PublicEventsApi {

    private final PublicFeedEventsUseCase publicFeedEventsService;
    private final FeedEventApiMapper eventApiMapper;

    public PublicFeedEventsController(
            final PublicFeedEventsUseCase publicFeedEventsService,
            final FeedEventApiMapper eventApiMapper
    ) {
        this.publicFeedEventsService = publicFeedEventsService;
        this.eventApiMapper = eventApiMapper;
    }

    @Override
    @GetMapping(
            value = PublicEventsApi.PATH_PUBLIC_EVENTS_LIST,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageEventSummary> publicEventsList(final Integer page, final Integer size) {
        final int safePage = page == null ? 0 : page;
        final int safeSize = size == null ? 20 : size;
        final String ifNoneMatch = getHeader("If-None-Match");
        final String ifModifiedSince = getHeader("If-Modified-Since");
        final var version = publicFeedEventsService.getFeedEventsVersion(safePage, safeSize);

        if (ifNoneMatch != null && !ifNoneMatch.isBlank()) {
            if (version.matchesEtag(ifNoneMatch)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .eTag(version.etag())
                        .lastModified(version.lastModifiedEpochMillis())
                        .build();
            }
        } else if (version.isNotModifiedSince(ifModifiedSince)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(version.etag())
                    .lastModified(version.lastModifiedEpochMillis())
                    .build();
        }

        final var items = publicFeedEventsService.listPublished(safePage, safeSize);
        return ResponseEntity.ok()
                .eTag(version.etag())
                .lastModified(version.lastModifiedEpochMillis())
                .body(eventApiMapper.toPageEventSummary(items, safePage, safeSize));
    }

    @Override
    @GetMapping(
            value = PublicEventsApi.PATH_PUBLIC_EVENT_GET_BY_SLUG,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EventDetails> publicEventGetBySlug(final String slug) {
        final var event = publicFeedEventsService.getBySlug(slug);
        return event.map(details -> ResponseEntity.ok(eventApiMapper.toEventDetails(details)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static String getHeader(final String name) {
        final ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        final HttpServletRequest request = attributes.getRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(name);
    }
}


