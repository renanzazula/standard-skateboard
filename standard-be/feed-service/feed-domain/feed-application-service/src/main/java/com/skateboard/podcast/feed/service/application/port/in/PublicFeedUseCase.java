package com.skateboard.podcast.feed.service.application.port.in;

import com.skateboard.podcast.feed.service.application.dto.FeedItemSummaryView;
import com.skateboard.podcast.feed.service.application.dto.FeedVersion;

import java.util.List;

public interface PublicFeedUseCase {
    List<FeedItemSummaryView> listPublished(int page, int size);

    FeedVersion getFeedVersion(int page, int size);
}
