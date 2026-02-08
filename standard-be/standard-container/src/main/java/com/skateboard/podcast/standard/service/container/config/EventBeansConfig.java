package com.skateboard.podcast.standard.service.container.config;

import com.skateboard.podcast.feed.service.events.application.port.in.AdminFeedEventsUseCase;
import com.skateboard.podcast.feed.service.events.application.port.in.PublicFeedEventsUseCase;
import com.skateboard.podcast.feed.service.events.application.port.out.FeedEventRepository;
import com.skateboard.podcast.feed.service.events.application.port.out.FeedEventsEventPublisher;
import com.skateboard.podcast.feed.service.events.application.service.AdminFeedEventsService;
import com.skateboard.podcast.feed.service.events.application.service.PublicFeedEventsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBeansConfig {

    @Bean
    public PublicFeedEventsUseCase publicFeedEventsService(final FeedEventRepository eventRepository) {
        return new PublicFeedEventsService(eventRepository);
    }

    @Bean
    public AdminFeedEventsUseCase adminFeedEventsService(
            final FeedEventRepository eventRepository,
            final FeedEventsEventPublisher eventsEventPublisher
    ) {
        return new AdminFeedEventsService(eventRepository, eventsEventPublisher);
    }
}


