package com.skateboard.podcast.settings.service.application.dto;

public record SessionConfigView(
        Integer maxTime,
        Integer idleTime,
        Boolean autoRefresh
) {}
