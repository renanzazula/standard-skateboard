package com.skateboard.podcast.standard.service.container.appconfig.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfigJpaEntity, Long> {
}
