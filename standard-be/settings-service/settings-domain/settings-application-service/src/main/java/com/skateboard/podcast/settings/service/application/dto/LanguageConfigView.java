package com.skateboard.podcast.settings.service.application.dto;

import java.util.List;

public record LanguageConfigView(
        List<LanguageView> availableLanguages,
        LanguageView defaultLanguage
) {}
