package com.skateboard.podcast.iam.service.dataaccess.persistence;

import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserStatus;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository.UserRecord;
import com.skateboard.podcast.iam.service.dataaccess.IamDataaccessTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = IamDataaccessTestConfig.class)
@Import(UserRepositoryAdapter.class)
class UserRepositoryAdapterTest {

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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserRepositoryAdapter adapter;

    @Test
    void saveAndFindByEmailAndId() {
        final UUID id = UUID.randomUUID();
        final UserRecord record = new UserRecord(
                id,
                Email.of("user@example.com"),
                "hashed",
                Role.USER,
                Provider.MANUAL,
                UserStatus.ACTIVE
        );

        adapter.save(record);

        final var byEmail = adapter.findByEmail(Email.of("user@example.com"));
        assertTrue(byEmail.isPresent());
        assertEquals(id, byEmail.get().id());

        final var byId = adapter.findById(id);
        assertTrue(byId.isPresent());
        assertEquals("user@example.com", byId.get().email().value());
    }
}
