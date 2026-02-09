package com.skateboard.podcast.standard.service.container.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.settings.service.application.port.in.SettingsConfigUseCase;
import com.skateboard.podcast.settings.service.application.port.out.SettingsConfigRepository;
import com.skateboard.podcast.settings.service.application.service.SettingsConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettingsConfigBeansConfig {

    @Bean
    public SettingsConfigUseCase settingsConfigUseCase(
            final SettingsConfigRepository settingsConfigRepository,
            final ObjectMapper objectMapper
    ) {
        return new SettingsConfigService(settingsConfigRepository, objectMapper);
    }
}
