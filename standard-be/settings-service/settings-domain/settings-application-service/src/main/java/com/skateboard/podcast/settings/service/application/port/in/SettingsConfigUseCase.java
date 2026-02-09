package com.skateboard.podcast.settings.service.application.port.in;

import com.skateboard.podcast.settings.service.application.dto.SettingsConfigView;

public interface SettingsConfigUseCase {

    SettingsConfigView get();

    SettingsConfigView update(SettingsConfigView updated);
}
