package com.skateboard.podcast.appconfig.service.application.adapter.in.rest;

import com.skateboard.podcast.appconfig.service.application.dto.AppConfigView;
import com.skateboard.podcast.appconfig.service.application.dto.SplashConfigView;
import com.skateboard.podcast.standardbe.api.model.AppConfig;
import com.skateboard.podcast.standardbe.api.model.SplashConfig;
import com.skateboard.podcast.standardbe.api.model.SplashMediaType;
import org.springframework.stereotype.Component;

@Component
public class AppConfigApiMapper {

    public AppConfig toApi(final AppConfigView view) {
        if (view == null) {
            return null;
        }
        return new AppConfig()
                .socialLoginEnabled(view.socialLoginEnabled())
                .postsPerPage(view.postsPerPage())
                .splash(toApi(view.splash()));
    }

    public AppConfigView toView(final AppConfig api) {
        if (api == null) {
            return null;
        }
        return new AppConfigView(
                api.getSocialLoginEnabled(),
                api.getPostsPerPage(),
                toView(api.getSplash())
        );
    }

    private SplashConfig toApi(final SplashConfigView view) {
        if (view == null) {
            return null;
        }
        return new SplashConfig()
                .enabled(view.enabled())
                .mediaType(SplashMediaType.fromValue(view.mediaType()))
                .mediaUrl(view.mediaUrl())
                .duration(view.duration())
                .showCloseButton(view.showCloseButton())
                .closeButtonDelay(view.closeButtonDelay());
    }

    private SplashConfigView toView(final SplashConfig api) {
        if (api == null) {
            return null;
        }
        final String mediaType = api.getMediaType() == null ? null : api.getMediaType().getValue();
        return new SplashConfigView(
                api.getEnabled(),
                mediaType,
                api.getMediaUrl(),
                api.getDuration(),
                api.getShowCloseButton(),
                api.getCloseButtonDelay()
        );
    }
}
