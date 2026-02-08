package com.skateboard.podcast.feed.service.application.port.in;

import com.skateboard.podcast.feed.service.application.dto.ImportPostCommand;
import com.skateboard.podcast.feed.service.application.dto.PostDetailsView;
import com.skateboard.podcast.feed.service.application.dto.PostSummaryView;
import com.skateboard.podcast.domain.valueobject.PostStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminPostsUseCase {
    List<PostSummaryView> list(int page, int size, PostStatus status);

    Optional<PostDetailsView> getById(UUID id);

    PostDetailsView createDraft(
            String title,
            String slug,
            String excerpt,
            List<String> tags,
            String thumbnailJson,
            String contentJson,
            UUID authorId
    );

    PostDetailsView updateById(
            UUID id,
            String title,
            String slug,
            String excerpt,
            List<String> tags,
            String thumbnailJson,
            String contentJson
    );

    PostDetailsView publishById(UUID id);

    void deleteById(UUID id);

    List<PostSummaryView> importPosts(List<ImportPostCommand> items, UUID authorId);

    void resetAll();
}

