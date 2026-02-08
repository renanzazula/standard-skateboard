package com.skateboard.podcast.standard.service.container.config;

import com.skateboard.podcast.feed.service.application.port.in.AdminPostsUseCase;
import com.skateboard.podcast.feed.service.application.service.AdminPostsService;
import com.skateboard.podcast.feed.service.application.port.out.FeedEventPublisher;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminBeansConfig {

    @Bean
    public AdminPostsUseCase adminPostsService(
            final PostRepository postRepository,
            final FeedEventPublisher feedEventPublisher
    ) {
        return new AdminPostsService(postRepository, feedEventPublisher);
    }
}

