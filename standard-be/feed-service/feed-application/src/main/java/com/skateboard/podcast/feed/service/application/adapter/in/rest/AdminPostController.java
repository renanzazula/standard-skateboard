package com.skateboard.podcast.feed.service.application.adapter.in.rest;

import com.skateboard.podcast.domain.security.CurrentUser;
import com.skateboard.podcast.feed.service.application.port.in.AdminPostUseCase;
import com.skateboard.podcast.standardbe.api.AdminPostsApi;
import com.skateboard.podcast.standardbe.api.model.CreatePostRequest;
import com.skateboard.podcast.standardbe.api.model.PostDetails;
import com.skateboard.podcast.standardbe.api.model.UpdatePostRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AdminPostController implements AdminPostsApi {

    private final AdminPostUseCase adminPostService;
    private final PostApiMapper postApiMapper;

    public AdminPostController(
            final AdminPostUseCase adminPostService,
            final PostApiMapper postApiMapper
    ) {
        this.adminPostService = adminPostService;
        this.postApiMapper = postApiMapper;
    }

    @Override
    public ResponseEntity<PostDetails> adminPostCreate(final CreatePostRequest createPostRequest) {
        final UUID authorId = requireUserId();
        final var created = adminPostService.createDraft(
                createPostRequest.getTitle(),
                createPostRequest.getSlug(),
                createPostRequest.getExcerpt(),
                createPostRequest.getTags(),
                postApiMapper.writeThumbnailJson(createPostRequest.getThumbnail()),
                postApiMapper.writeContentJson(createPostRequest.getContent()),
                authorId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(postApiMapper.toPostDetails(created));
    }

    @Override
    public ResponseEntity<PostDetails> adminPostUpdate(final UUID id, final UpdatePostRequest updatePostRequest) {
        final var updated = adminPostService.updateById(
                id,
                updatePostRequest.getTitle(),
                updatePostRequest.getSlug(),
                updatePostRequest.getExcerpt(),
                updatePostRequest.getTags(),
                postApiMapper.writeThumbnailJson(updatePostRequest.getThumbnail()),
                postApiMapper.writeContentJson(updatePostRequest.getContent())
        );
        return ResponseEntity.ok(postApiMapper.toPostDetails(updated));
    }

    @Override
    public ResponseEntity<PostDetails> adminPostPublish(final UUID id) {
        final var updated = adminPostService.publishById(id);
        return ResponseEntity.ok(postApiMapper.toPostDetails(updated));
    }

    private static UUID requireUserId() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }
        if (auth.getPrincipal() instanceof final CurrentUser currentUser) {
            return currentUser.userId();
        }
        throw new IllegalStateException("No authenticated user");
    }
}
