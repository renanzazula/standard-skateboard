package com.skateboard.podcast.appconfig.service.application.dto;

public record SplashConfigView(
        Boolean enabled,
        String mediaType,
        String mediaUrl,
        Integer duration,
        Boolean showCloseButton,
        Integer closeButtonDelay
) {}
