package com.skateboard.podcast.standard.service.container.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.skateboard.podcast.iam.service.dataaccess.persistence.jpa",
        "com.skateboard.podcast.feed.service.dataaccess.persistence.jpa",
        "com.skateboard.podcast.feed.service.events.dataaccess.persistence.jpa",
        "com.skateboard.podcast.appconfig.service.dataaccess.persistence.jpa",
        "com.skateboard.podcast.settings.service.dataaccess.persistence.jpa"
})
@EntityScan(basePackages = {
        "com.skateboard.podcast.iam.service.dataaccess.persistence.jpa",
        "com.skateboard.podcast.feed.service.dataaccess.persistence.jpa",
        "com.skateboard.podcast.feed.service.events.dataaccess.persistence.jpa",
        "com.skateboard.podcast.appconfig.service.dataaccess.persistence.jpa",
        "com.skateboard.podcast.settings.service.dataaccess.persistence.jpa"
})
public class JpaConfig {
}

