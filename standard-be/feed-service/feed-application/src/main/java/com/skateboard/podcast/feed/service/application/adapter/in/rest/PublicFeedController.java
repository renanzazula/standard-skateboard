package com.skateboard.podcast.feed.service.application.adapter.in.rest;

import com.skateboard.podcast.feed.service.application.port.in.PublicFeedUseCase;
import com.skateboard.podcast.standardbe.api.PublicFeedApi;
import com.skateboard.podcast.standardbe.api.model.PagePostSummary;
import com.skateboard.podcast.standardbe.api.model.PostDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class PublicFeedController implements PublicFeedApi {

    private final PublicFeedUseCase publicFeedService;
    private final PostApiMapper postApiMapper;

    public PublicFeedController(
            final PublicFeedUseCase publicFeedService,
            final PostApiMapper postApiMapper
    ) {
        this.publicFeedService = publicFeedService;
        this.postApiMapper = postApiMapper;
    }

    @Override
    @GetMapping(
            value = PublicFeedApi.PATH_PUBLIC_FEED_LIST,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PagePostSummary> publicFeedList(
            final Integer page,
            final Integer size
    ) {
        final int safePage = page == null ? 0 : page;
        final int safeSize = size == null ? 20 : size;
        final String ifNoneMatch = getHeader("If-None-Match");
        final String ifModifiedSince = getHeader("If-Modified-Since");
        final var version = publicFeedService.getFeedVersion(safePage, safeSize);

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

        final var items = publicFeedService.listPublished(safePage, safeSize);
        return ResponseEntity.ok()
                .eTag(version.etag())
                .lastModified(version.lastModifiedEpochMillis())
                .body(postApiMapper.toPagePostSummary(items, safePage, safeSize));
    }

    @Override
    @GetMapping(
            value = PublicFeedApi.PATH_PUBLIC_POST_GET_BY_SLUG,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PostDetails> publicPostGetBySlug(final String slug) {
        final var post = publicFeedService.getBySlug(slug);
        return post.map(details -> ResponseEntity.ok(postApiMapper.toPostDetails(details)))
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
