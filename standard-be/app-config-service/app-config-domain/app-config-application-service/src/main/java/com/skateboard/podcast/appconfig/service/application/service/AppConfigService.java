package com.skateboard.podcast.appconfig.service.application.service;

import com.skateboard.podcast.appconfig.service.application.dto.AppConfigView;
import com.skateboard.podcast.appconfig.service.application.dto.SplashConfigView;
import com.skateboard.podcast.appconfig.service.application.port.in.AppConfigUseCase;
import com.skateboard.podcast.appconfig.service.application.port.out.AppConfigEventPublisher;
import com.skateboard.podcast.appconfig.service.application.port.out.AppConfigRepository;
import com.skateboard.podcast.domain.exception.ValidationException;

import java.time.Instant;

public class AppConfigService implements AppConfigUseCase {

    private static final long CONFIG_ID = 1L;
    private static final int MIN_POSTS_PER_PAGE = 5;
    private static final int MAX_POSTS_PER_PAGE = 50;

    private final AppConfigRepository repository;
    private final AppConfigEventPublisher eventPublisher;

    public AppConfigService(
            final AppConfigRepository repository,
            final AppConfigEventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AppConfigView get() {
        return toView(loadOrCreate());
    }

    @Override
    public AppConfigView update(final AppConfigView updated) {
        validate(updated);
        final AppConfigRepository.AppConfigRecord existing = loadOrCreate();
        final AppConfigRepository.AppConfigRecord saved = repository.save(
                toRecord(existing.id(), updated, existing.updatedAt())
        );
        eventPublisher.publishConfigUpdated(Instant.now());
        return toView(saved);
    }

    private AppConfigRepository.AppConfigRecord loadOrCreate() {
        return repository.findById(CONFIG_ID)
                .orElseGet(this::createDefault);
    }

    private AppConfigRepository.AppConfigRecord createDefault() {
        final AppConfigView defaultConfig = defaultConfig();
        final AppConfigRepository.AppConfigRecord record = toRecord(CONFIG_ID, defaultConfig, null);
        return repository.save(record);
    }

    private AppConfigView defaultConfig() {
        return new AppConfigView(
                false,
                20,
                new SplashConfigView(
                        false,
                        "image",
                        null,
                        0,
                        true,
                        0
                )
        );
    }

    private static void validate(final AppConfigView config) {
        if (config == null) {
            throw new ValidationException("app config cannot be null");
        }
        if (config.socialLoginEnabled() == null) {
            throw new ValidationException("socialLoginEnabled cannot be null");
        }
        if (config.postsPerPage() == null) {
            throw new ValidationException("postsPerPage cannot be null");
        }
        final int postsPerPage = config.postsPerPage();
        if (postsPerPage < MIN_POSTS_PER_PAGE || postsPerPage > MAX_POSTS_PER_PAGE) {
            throw new ValidationException("postsPerPage out of range");
        }

        final SplashConfigView splash = config.splash();
        if (splash == null) {
            throw new ValidationException("splash cannot be null");
        }
        if (splash.enabled() == null) {
            throw new ValidationException("splash.enabled cannot be null");
        }
        if (splash.mediaType() == null) {
            throw new ValidationException("splash.mediaType cannot be null");
        }
        if (splash.duration() == null || splash.duration() < 0) {
            throw new ValidationException("splash.duration cannot be negative");
        }
        if (splash.showCloseButton() == null) {
            throw new ValidationException("splash.showCloseButton cannot be null");
        }
        if (splash.closeButtonDelay() == null || splash.closeButtonDelay() < 0) {
            throw new ValidationException("splash.closeButtonDelay cannot be negative");
        }
    }

    private static AppConfigRepository.AppConfigRecord toRecord(
            final long id,
            final AppConfigView source,
            final Instant updatedAt
    ) {
        return new AppConfigRepository.AppConfigRecord(
                id,
                source.socialLoginEnabled(),
                source.postsPerPage(),
                source.splash().enabled(),
                source.splash().mediaType(),
                source.splash().mediaUrl(),
                source.splash().duration(),
                source.splash().showCloseButton(),
                source.splash().closeButtonDelay(),
                updatedAt
        );
    }

    private static AppConfigView toView(final AppConfigRepository.AppConfigRecord record) {
        if (record == null) {
            return null;
        }
        return new AppConfigView(
                record.socialLoginEnabled(),
                record.postsPerPage(),
                new SplashConfigView(
                        record.splashEnabled(),
                        record.splashMediaType(),
                        record.splashMediaUrl(),
                        record.splashDuration(),
                        record.splashShowCloseButton(),
                        record.splashCloseButtonDelay()
                )
        );
    }
}
