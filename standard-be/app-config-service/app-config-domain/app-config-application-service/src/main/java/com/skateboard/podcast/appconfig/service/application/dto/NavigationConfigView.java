package com.skateboard.podcast.appconfig.service.application.dto;

import java.util.List;

public record NavigationConfigView(
        List<NavigationTabView> tabs
) {}
