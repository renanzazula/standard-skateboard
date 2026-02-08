package com.skateboard.podcast.appconfig.service.application.port.in;

import com.skateboard.podcast.appconfig.service.application.dto.NavigationConfigView;

public interface NavigationConfigUseCase {
    NavigationConfigView get();

    NavigationConfigView update(NavigationConfigView config);
}
