package com.skateboard.podcast.standard.service.container.appconfig;

import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.standardbe.api.model.AppConfig;
import com.skateboard.podcast.standardbe.api.model.SplashConfig;
import com.skateboard.podcast.standardbe.api.model.SplashMediaType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AppConfigStore {

    private static final int MIN_POSTS_PER_PAGE = 5;
    private static final int MAX_POSTS_PER_PAGE = 50;

    private final AppConfigEventPublisher eventPublisher;
    private final AtomicReference<AppConfig> current =
            new AtomicReference<>(defaultConfig());

    public AppConfigStore(final AppConfigEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public AppConfig get() {
        return copy(current.get());
    }

    public AppConfig update(final AppConfig updated) {
        validate(updated);
        current.set(copy(updated));
        eventPublisher.publishConfigUpdated(Instant.now());
        return get();
    }

    private static AppConfig defaultConfig() {
        return new AppConfig()
                .socialLoginEnabled(false)
                .postsPerPage(20)
                .splash(new SplashConfig()
                        .enabled(false)
                        .mediaType(SplashMediaType.IMAGE)
                        .mediaUrl(null)
                        .duration(0)
                        .showCloseButton(true)
                        .closeButtonDelay(0));
    }

    private static void validate(final AppConfig config) {
        if (config == null) {
            throw new ValidationException("app config cannot be null");
        }
        if (config.getSocialLoginEnabled() == null) {
            throw new ValidationException("socialLoginEnabled cannot be null");
        }
        if (config.getPostsPerPage() == null) {
            throw new ValidationException("postsPerPage cannot be null");
        }
        final int postsPerPage = config.getPostsPerPage();
        if (postsPerPage < MIN_POSTS_PER_PAGE || postsPerPage > MAX_POSTS_PER_PAGE) {
            throw new ValidationException("postsPerPage out of range");
        }

        final SplashConfig splash = config.getSplash();
        if (splash == null) {
            throw new ValidationException("splash cannot be null");
        }
        if (splash.getEnabled() == null) {
            throw new ValidationException("splash.enabled cannot be null");
        }
        if (splash.getMediaType() == null) {
            throw new ValidationException("splash.mediaType cannot be null");
        }
        if (splash.getDuration() == null || splash.getDuration() < 0) {
            throw new ValidationException("splash.duration cannot be negative");
        }
        if (splash.getShowCloseButton() == null) {
            throw new ValidationException("splash.showCloseButton cannot be null");
        }
        if (splash.getCloseButtonDelay() == null || splash.getCloseButtonDelay() < 0) {
            throw new ValidationException("splash.closeButtonDelay cannot be negative");
        }
    }

    private static AppConfig copy(final AppConfig source) {
        if (source == null) {
            return null;
        }
        return new AppConfig()
                .socialLoginEnabled(source.getSocialLoginEnabled())
                .postsPerPage(source.getPostsPerPage())
                .splash(copy(source.getSplash()));
    }

    private static SplashConfig copy(final SplashConfig source) {
        if (source == null) {
            return null;
        }
        return new SplashConfig()
                .enabled(source.getEnabled())
                .mediaType(source.getMediaType())
                .mediaUrl(source.getMediaUrl())
                .duration(source.getDuration())
                .showCloseButton(source.getShowCloseButton())
                .closeButtonDelay(source.getCloseButtonDelay());
    }
}
