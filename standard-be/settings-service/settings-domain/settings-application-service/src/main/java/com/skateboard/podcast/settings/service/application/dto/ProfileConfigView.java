package com.skateboard.podcast.settings.service.application.dto;

import java.util.List;

public record ProfileConfigView(
        Integer usernameMinLength,
        Integer usernameMaxLength,
        Integer avatarMaxSizeMB,
        List<String> allowedAvatarFormats
) {}
