package com.skateboard.podcast.feed.service.application.adapter.in.rest;

import com.skateboard.podcast.feed.service.application.port.in.PublicFeedUseCase;
import com.skateboard.podcast.standardbe.api.PublicFeedApi;
import com.skateboard.podcast.standardbe.api.model.PagePostSummary;
import com.skateboard.podcast.standardbe.api.model.PostDetails;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<PagePostSummary> publicFeedList(final Integer page, final Integer size) {
        final int safePage = page == null ? 0 : page;
        final int safeSize = size == null ? 20 : size;
        final var items = publicFeedService.listPublished(safePage, safeSize);
        return ResponseEntity.ok(postApiMapper.toPagePostSummary(items, safePage, safeSize));
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
}
