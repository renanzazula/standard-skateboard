package com.skateboard.podcast.standard.service.container.appconfig;

import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.standardbe.api.model.AppConfig;
import com.skateboard.podcast.standard.service.container.appconfig.persistence.jpa.AppConfigJpaEntity;
import com.skateboard.podcast.standard.service.container.appconfig.persistence.jpa.AppConfigRepository;
import com.skateboard.podcast.standardbe.api.model.SplashConfig;
import com.skateboard.podcast.standardbe.api.model.SplashMediaType;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AppConfigStore {

    private static final long CONFIG_ID = 1L;
    private static final int MIN_POSTS_PER_PAGE = 5;
    private static final int MAX_POSTS_PER_PAGE = 50;

    private final AppConfigRepository repository;
    private final AppConfigEventPublisher eventPublisher;

    public AppConfigStore(
            final AppConfigRepository repository,
            final AppConfigEventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public AppConfig get() {
        return toApiConfig(loadOrCreate());
    }

    public AppConfig update(final AppConfig updated) {
        validate(updated);
        final AppConfigJpaEntity entity = toEntity(updated, loadOrCreate());
        repository.save(entity);
        eventPublisher.publishConfigUpdated(Instant.now());
        return toApiConfig(entity);
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

    private AppConfigJpaEntity loadOrCreate() {
        return repository.findById(CONFIG_ID)
                .orElseGet(this::createDefault);
    }

    private AppConfigJpaEntity createDefault() {
        final AppConfigJpaEntity entity = toEntity(defaultConfig(), new AppConfigJpaEntity());
        entity.setId(CONFIG_ID);
        return repository.save(entity);
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

    private static AppConfigJpaEntity toEntity(final AppConfig source, final AppConfigJpaEntity target) {
        target.setSocialLoginEnabled(source.getSocialLoginEnabled());
        target.setPostsPerPage(source.getPostsPerPage());
        target.setSplashEnabled(source.getSplash().getEnabled());
        target.setSplashMediaType(source.getSplash().getMediaType().getValue());
        target.setSplashMediaUrl(source.getSplash().getMediaUrl());
        target.setSplashDuration(source.getSplash().getDuration());
        target.setSplashShowCloseButton(source.getSplash().getShowCloseButton());
        target.setSplashCloseButtonDelay(source.getSplash().getCloseButtonDelay());
        return target;
    }

    private static AppConfig toApiConfig(final AppConfigJpaEntity entity) {
        if (entity == null) {
            return defaultConfig();
        }
        return new AppConfig()
                .socialLoginEnabled(entity.getSocialLoginEnabled())
                .postsPerPage(entity.getPostsPerPage())
                .splash(new SplashConfig()
                        .enabled(entity.getSplashEnabled())
                        .mediaType(SplashMediaType.fromValue(entity.getSplashMediaType()))
                        .mediaUrl(entity.getSplashMediaUrl())
                        .duration(entity.getSplashDuration())
                        .showCloseButton(entity.getSplashShowCloseButton())
                        .closeButtonDelay(entity.getSplashCloseButtonDelay()));
    }
}
