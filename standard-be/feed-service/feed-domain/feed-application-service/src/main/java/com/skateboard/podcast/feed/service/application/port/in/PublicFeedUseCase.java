package com.skateboard.podcast.feed.service.application.port.in;

import com.skateboard.podcast.feed.service.application.dto.PostDetailsView;
import com.skateboard.podcast.feed.service.application.dto.PostSummaryView;

import java.util.List;
import java.util.Optional;

public interface PublicFeedUseCase {
    List<PostSummaryView> listPublished(int page, int size);

    Optional<PostDetailsView> getBySlug(String slug);
}
