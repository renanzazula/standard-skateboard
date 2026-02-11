package com.skateboard.podcast.settings.service.application.dto;

public record SettingsConfigView(
        SettingsAuthMethodsView enabledAuthMethods,
        SettingsServiceModesView serviceModes,
        SessionConfigView sessionConfig,
        LanguageConfigView languageConfig,
        ProfileConfigView profileConfig,
        Boolean feedRealtimeEnabled
) {}
