package com.skateboard.podcast.appconfig.service.dataaccess.persistence;

import com.skateboard.podcast.appconfig.service.application.port.out.AppConfigRepository;
import com.skateboard.podcast.appconfig.service.dataaccess.persistence.jpa.AppConfigJpaEntity;
import com.skateboard.podcast.appconfig.service.dataaccess.persistence.jpa.SpringDataAppConfigRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AppConfigRepositoryAdapter implements AppConfigRepository {

    private final SpringDataAppConfigRepository repo;

    public AppConfigRepositoryAdapter(final SpringDataAppConfigRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<AppConfigRecord> findById(final long id) {
        return repo.findById(id).map(AppConfigRepositoryAdapter::toRecord);
    }

    @Override
    public AppConfigRecord save(final AppConfigRecord record) {
        final AppConfigJpaEntity entity = new AppConfigJpaEntity();
        entity.setId(record.id());
        entity.setSocialLoginEnabled(record.socialLoginEnabled());
        entity.setPostsPerPage(record.postsPerPage());
        entity.setSplashEnabled(record.splashEnabled());
        entity.setSplashMediaType(record.splashMediaType());
        entity.setSplashMediaUrl(record.splashMediaUrl());
        entity.setSplashDuration(record.splashDuration());
        entity.setSplashShowCloseButton(record.splashShowCloseButton());
        entity.setSplashCloseButtonDelay(record.splashCloseButtonDelay());
        entity.setUpdatedAt(record.updatedAt());
        repo.save(entity);
        return record;
    }

    private static AppConfigRecord toRecord(final AppConfigJpaEntity entity) {
        return new AppConfigRecord(
                entity.getId(),
                entity.getSocialLoginEnabled(),
                entity.getPostsPerPage(),
                entity.getSplashEnabled(),
                entity.getSplashMediaType(),
                entity.getSplashMediaUrl(),
                entity.getSplashDuration(),
                entity.getSplashShowCloseButton(),
                entity.getSplashCloseButtonDelay(),
                entity.getUpdatedAt()
        );
    }
}
