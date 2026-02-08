package com.skateboard.podcast.standard.service.container.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.appconfig.service.application.port.in.AppConfigUseCase;
import com.skateboard.podcast.appconfig.service.application.port.in.NavigationConfigUseCase;
import com.skateboard.podcast.appconfig.service.application.port.out.AppConfigEventPublisher;
import com.skateboard.podcast.appconfig.service.application.port.out.AppConfigRepository;
import com.skateboard.podcast.appconfig.service.application.port.out.NavigationConfigEventPublisher;
import com.skateboard.podcast.appconfig.service.application.port.out.NavigationConfigRepository;
import com.skateboard.podcast.appconfig.service.application.service.AppConfigService;
import com.skateboard.podcast.appconfig.service.application.service.NavigationConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfigBeansConfig {

    @Bean
    public AppConfigUseCase appConfigUseCase(
            final AppConfigRepository appConfigRepository,
            final AppConfigEventPublisher appConfigEventPublisher
    ) {
        return new AppConfigService(appConfigRepository, appConfigEventPublisher);
    }

    @Bean
    public NavigationConfigUseCase navigationConfigUseCase(
            final NavigationConfigRepository navigationConfigRepository,
            final NavigationConfigEventPublisher navigationConfigEventPublisher,
            final ObjectMapper objectMapper
    ) {
        return new NavigationConfigService(navigationConfigRepository, navigationConfigEventPublisher, objectMapper);
    }
}
