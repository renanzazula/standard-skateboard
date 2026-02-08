package com.skateboard.podcast.appconfig.service.dataaccess.persistence;

import com.skateboard.podcast.appconfig.service.application.port.out.NavigationConfigRepository;
import com.skateboard.podcast.appconfig.service.dataaccess.persistence.jpa.NavigationConfigJpaEntity;
import com.skateboard.podcast.appconfig.service.dataaccess.persistence.jpa.SpringDataNavigationConfigRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NavigationConfigRepositoryAdapter implements NavigationConfigRepository {

    private final SpringDataNavigationConfigRepository repo;

    public NavigationConfigRepositoryAdapter(final SpringDataNavigationConfigRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<NavigationConfigRecord> findById(final long id) {
        return repo.findById(id).map(NavigationConfigRepositoryAdapter::toRecord);
    }

    @Override
    public NavigationConfigRecord save(final NavigationConfigRecord record) {
        final NavigationConfigJpaEntity entity = new NavigationConfigJpaEntity();
        entity.setId(record.id());
        entity.setConfigJson(record.configJson());
        entity.setUpdatedAt(record.updatedAt());
        repo.save(entity);
        return record;
    }

    private static NavigationConfigRecord toRecord(final NavigationConfigJpaEntity entity) {
        return new NavigationConfigRecord(
                entity.getId(),
                entity.getConfigJson(),
                entity.getUpdatedAt()
        );
    }
}
