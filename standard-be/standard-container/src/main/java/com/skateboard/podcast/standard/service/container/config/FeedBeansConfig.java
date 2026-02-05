package com.skateboard.podcast.standard.service.container.config;

import com.skateboard.podcast.feed.service.application.port.in.PublicFeedUseCase;
import com.skateboard.podcast.feed.service.application.service.PublicFeedService;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeedBeansConfig {

    @Bean
    public PublicFeedUseCase publicFeedService(final PostRepository postRepository) {
        return new PublicFeedService(postRepository);
    }
}
