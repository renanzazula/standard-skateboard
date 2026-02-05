package com.skateboard.podcast.feed.service.application.port.in;

import com.skateboard.podcast.feed.service.application.dto.PostDetailsView;

import java.util.List;
import java.util.UUID;

public interface AdminPostUseCase {
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
}
