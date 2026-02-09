package com.skateboard.podcast.settings.service.application.dto;

public record SettingsAuthMethodsView(
        Boolean google,
        Boolean apple,
        Boolean manual
) {}
