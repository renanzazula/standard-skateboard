package com.skateboard.podcast.settings.service.dataaccess.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSettingsConfigRepository extends JpaRepository<SettingsConfigJpaEntity, Long> {
}
