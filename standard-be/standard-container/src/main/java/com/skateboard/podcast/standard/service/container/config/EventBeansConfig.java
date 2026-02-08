package com.skateboard.podcast.standard.service.container.config;

import com.skateboard.podcast.feed.service.events.application.port.in.AdminFeedEventsUseCase;
import com.skateboard.podcast.feed.service.events.application.port.in.PublicFeedEventsUseCase;
import com.skateboard.podcast.feed.service.events.application.port.out.EventRepository;
import com.skateboard.podcast.feed.service.events.application.port.out.EventsEventPublisher;
import com.skateboard.podcast.feed.service.events.application.service.AdminFeedEventsService;
import com.skateboard.podcast.feed.service.events.application.service.PublicFeedEventsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBeansConfig {

    @Bean
    public PublicFeedEventsUseCase publicFeedEventsService(final EventRepository eventRepository) {
        return new PublicFeedEventsService(eventRepository);
    }

    @Bean
    public AdminFeedEventsUseCase adminFeedEventsService(
            final EventRepository eventRepository,
            final EventsEventPublisher eventsEventPublisher
    ) {
        return new AdminFeedEventsService(eventRepository, eventsEventPublisher);
    }
}


