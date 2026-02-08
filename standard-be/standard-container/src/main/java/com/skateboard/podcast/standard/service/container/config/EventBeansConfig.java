package com.skateboard.podcast.standard.service.container.config;

import com.skateboard.podcast.event.service.application.port.in.AdminEventUseCase;
import com.skateboard.podcast.event.service.application.port.in.PublicEventsUseCase;
import com.skateboard.podcast.event.service.application.port.out.EventRepository;
import com.skateboard.podcast.event.service.application.port.out.EventsEventPublisher;
import com.skateboard.podcast.event.service.application.service.AdminEventService;
import com.skateboard.podcast.event.service.application.service.PublicEventsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBeansConfig {

    @Bean
    public PublicEventsUseCase publicEventsService(final EventRepository eventRepository) {
        return new PublicEventsService(eventRepository);
    }

    @Bean
    public AdminEventUseCase adminEventService(
            final EventRepository eventRepository,
            final EventsEventPublisher eventsEventPublisher
    ) {
        return new AdminEventService(eventRepository, eventsEventPublisher);
    }
}
