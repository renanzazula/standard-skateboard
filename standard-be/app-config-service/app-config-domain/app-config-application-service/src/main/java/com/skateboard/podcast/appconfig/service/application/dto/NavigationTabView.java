package com.skateboard.podcast.appconfig.service.application.dto;

public record NavigationTabView(
        String id,
        String name,
        String icon,
        Integer order,
        Boolean enabled,
        Boolean isSystem
) {}
