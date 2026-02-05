package com.skateboard.podcast.feed.service.dataaccess;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan("com.skateboard.podcast.feed.service.dataaccess.persistence.jpa")
@EnableJpaRepositories("com.skateboard.podcast.feed.service.dataaccess.persistence.jpa")
public class FeedDataaccessTestConfig {
}
