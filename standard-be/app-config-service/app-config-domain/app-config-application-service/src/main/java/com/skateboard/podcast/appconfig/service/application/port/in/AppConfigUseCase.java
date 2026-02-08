package com.skateboard.podcast.appconfig.service.application.port.in;

import com.skateboard.podcast.appconfig.service.application.dto.AppConfigView;

public interface AppConfigUseCase {
    AppConfigView get();

    AppConfigView update(AppConfigView config);
}
