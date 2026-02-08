package com.skateboard.podcast.standard.service.container.config;

import com.skateboard.podcast.feed.service.application.port.in.PublicFeedUseCase;
import com.skateboard.podcast.feed.service.application.port.in.PublicPostsUseCase;
import com.skateboard.podcast.feed.service.application.service.PublicFeedService;
import com.skateboard.podcast.feed.service.application.service.PublicPostsService;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import com.skateboard.podcast.feed.service.events.application.port.out.EventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeedBeansConfig {

    @Bean
    public PublicFeedUseCase publicFeedService(
            final PostRepository postRepository,
            final EventRepository eventRepository
    ) {
        return new PublicFeedService(postRepository, eventRepository);
    }

    @Bean
    public PublicPostsUseCase publicPostsService(final PostRepository postRepository) {
        return new PublicPostsService(postRepository);
    }
}

