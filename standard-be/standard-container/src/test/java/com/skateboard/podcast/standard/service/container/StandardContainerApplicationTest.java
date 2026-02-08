package com.skateboard.podcast.standard.service.container;

import com.skateboard.podcast.feed.service.application.adapter.in.rest.AdminPostsController;
import com.skateboard.podcast.feed.service.application.adapter.in.rest.PublicFeedController;
import com.skateboard.podcast.iam.service.application.adapter.in.rest.PublicAuthController;
import com.skateboard.podcast.iam.service.dataaccess.persistence.jpa.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StandardContainerApplicationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("standarddb")
            .withUsername("standard")
            .withPassword("standard");

    @DynamicPropertySource
    static void registerProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsWithHttpAndRepositories() {
        assertNotNull(context.getBean(PublicAuthController.class));
        assertNotNull(context.getBean(PublicFeedController.class));
        assertNotNull(context.getBean(AdminPostsController.class));
        assertNotNull(context.getBean(SpringDataUserRepository.class));
    }
}

