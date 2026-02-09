package com.skateboard.podcast.settings.service.dataaccess.persistence;

import com.skateboard.podcast.settings.service.application.port.out.SettingsConfigRepository;
import com.skateboard.podcast.settings.service.dataaccess.persistence.jpa.SettingsConfigJpaEntity;
import com.skateboard.podcast.settings.service.dataaccess.persistence.jpa.SpringDataSettingsConfigRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SettingsConfigRepositoryAdapter implements SettingsConfigRepository {

    private final SpringDataSettingsConfigRepository repo;

    public SettingsConfigRepositoryAdapter(final SpringDataSettingsConfigRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<SettingsConfigRecord> findById(final long id) {
        return repo.findById(id).map(SettingsConfigRepositoryAdapter::toRecord);
    }

    @Override
    public SettingsConfigRecord save(final SettingsConfigRecord record) {
        final SettingsConfigJpaEntity entity = new SettingsConfigJpaEntity();
        entity.setId(record.id());
        entity.setConfigJson(record.configJson());
        entity.setUpdatedAt(record.updatedAt());
        repo.save(entity);
        return record;
    }

    private static SettingsConfigRecord toRecord(final SettingsConfigJpaEntity entity) {
        return new SettingsConfigRecord(
                entity.getId(),
                entity.getConfigJson(),
                entity.getUpdatedAt()
        );
    }
}
