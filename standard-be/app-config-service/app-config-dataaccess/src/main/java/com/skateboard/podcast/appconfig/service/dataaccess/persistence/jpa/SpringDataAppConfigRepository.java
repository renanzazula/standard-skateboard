package com.skateboard.podcast.appconfig.service.dataaccess.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAppConfigRepository extends JpaRepository<AppConfigJpaEntity, Long> {
}
