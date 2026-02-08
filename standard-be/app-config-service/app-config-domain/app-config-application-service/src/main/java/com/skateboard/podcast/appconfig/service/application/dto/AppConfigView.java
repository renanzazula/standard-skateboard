package com.skateboard.podcast.appconfig.service.application.dto;

public record AppConfigView(
        Boolean socialLoginEnabled,
        Integer postsPerPage,
        SplashConfigView splash
) {}
