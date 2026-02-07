package com.skateboard.podcast.feed.service.application.adapter.in.rest;

import com.skateboard.podcast.domain.security.CurrentUser;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.feed.service.application.port.in.AdminPostUseCase;
import com.skateboard.podcast.standardbe.api.AdminPostsApi;
import com.skateboard.podcast.standardbe.api.model.Block;
import com.skateboard.podcast.standardbe.api.model.BlockType;
import com.skateboard.podcast.standardbe.api.model.CreatePostRequest;
import com.skateboard.podcast.standardbe.api.model.ImageRef;
import com.skateboard.podcast.standardbe.api.model.ImportPostsRequest;
import com.skateboard.podcast.standardbe.api.model.PagePostSummary;
import com.skateboard.podcast.standardbe.api.model.ParagraphBlock;
import com.skateboard.podcast.standardbe.api.model.PostDetails;
import com.skateboard.podcast.standardbe.api.model.PostStatus;
import com.skateboard.podcast.standardbe.api.model.PostSummary;
import com.skateboard.podcast.standardbe.api.model.PodcastImportItem;
import com.skateboard.podcast.standardbe.api.model.UpdatePostRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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
    @PostMapping(
            value = AdminPostsApi.PATH_ADMIN_POST_CREATE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
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
    @GetMapping(
            value = AdminPostsApi.PATH_ADMIN_POST_CREATE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PagePostSummary> adminPostList(
            final Integer page,
            final Integer size,
            final PostStatus status
    ) {
        final int safePage = page == null ? 0 : page;
        final int safeSize = size == null ? 20 : size;
        final var domainStatus = status == null
                ? null
                : com.skateboard.podcast.domain.valueobject.PostStatus.from(status.name());
        final var items = adminPostService.list(safePage, safeSize, domainStatus);
        return ResponseEntity.ok(postApiMapper.toPagePostSummary(items, safePage, safeSize));
    }

    @Override
    @GetMapping(
            value = AdminPostsApi.PATH_ADMIN_POST_UPDATE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PostDetails> adminPostGet(final UUID id) {
        return adminPostService.getById(id)
                .map(details -> ResponseEntity.ok(postApiMapper.toPostDetails(details)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @PutMapping(
            value = AdminPostsApi.PATH_ADMIN_POST_UPDATE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
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
    @PostMapping(
            value = AdminPostsApi.PATH_ADMIN_POST_PUBLISH,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PostDetails> adminPostPublish(final UUID id) {
        final var updated = adminPostService.publishById(id);
        return ResponseEntity.ok(postApiMapper.toPostDetails(updated));
    }

    @Override
    @DeleteMapping(value = AdminPostsApi.PATH_ADMIN_POST_UPDATE)
    public ResponseEntity<Void> adminPostDelete(final UUID id) {
        adminPostService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping(
            value = "/admin/posts/import",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<PostSummary>> adminPostImport(final ImportPostsRequest importPostsRequest) {
        final UUID authorId = requireUserId();
        final List<com.skateboard.podcast.feed.service.application.dto.ImportPostCommand> commands =
                toImportCommands(importPostsRequest);
        final var imported = adminPostService.importPosts(commands, authorId);
        final List<PostSummary> response = imported.stream()
                .map(postApiMapper::toPostSummary)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping(value = "/admin/posts/reset")
    public ResponseEntity<Void> adminPostReset() {
        adminPostService.resetAll();
        return ResponseEntity.noContent().build();
    }

    private List<com.skateboard.podcast.feed.service.application.dto.ImportPostCommand> toImportCommands(
            final ImportPostsRequest request
    ) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return List.of();
        }
        final List<com.skateboard.podcast.feed.service.application.dto.ImportPostCommand> commands =
                new ArrayList<>(request.getItems().size());
        for (final PodcastImportItem item : request.getItems()) {
            final String excerpt = item.getDescription();
            final String contentJson = postApiMapper.writeContentJson(buildImportContent(item));
            final String thumbnailJson = postApiMapper.writeThumbnailJson(buildThumbnail(item));
            commands.add(new com.skateboard.podcast.feed.service.application.dto.ImportPostCommand(
                    item.getTitle(),
                    excerpt,
                    thumbnailJson,
                    contentJson,
                    parsePublishedAt(item.getDate())
            ));
        }
        return commands;
    }

    private static List<Block> buildImportContent(final PodcastImportItem item) {
        final String text = firstNonBlank(item.getDescription(), item.getUrl(), item.getTitle());
        final ParagraphBlock block = new ParagraphBlock();
        block.setType(BlockType.PARAGRAPH);
        block.setText(text);
        return List.of(block);
    }

    private static ImageRef buildThumbnail(final PodcastImportItem item) {
        if (item == null || item.getThumbnail() == null || item.getThumbnail().isBlank()) {
            return null;
        }
        return new ImageRef()
                .url(item.getThumbnail())
                .alt(item.getTitle());
    }

    private static Instant parsePublishedAt(final String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(raw);
        } catch (final DateTimeParseException ignored) {
            try {
                return LocalDate.parse(raw).atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (final DateTimeParseException e) {
                throw new ValidationException("invalid date: " + raw);
            }
        }
    }

    private static String firstNonBlank(final String... values) {
        if (values == null) {
            return "";
        }
        for (final String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
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
