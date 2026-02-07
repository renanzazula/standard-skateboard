package com.skateboard.podcast.standard.service.container.config;

import com.skateboard.podcast.feed.service.application.port.in.AdminPostUseCase;
import com.skateboard.podcast.feed.service.application.service.AdminPostService;
import com.skateboard.podcast.feed.service.application.port.out.FeedEventPublisher;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminBeansConfig {

    @Bean
    public AdminPostUseCase adminPostService(
            final PostRepository postRepository,
            final FeedEventPublisher feedEventPublisher
    ) {
        return new AdminPostService(postRepository, feedEventPublisher);
    }
}
