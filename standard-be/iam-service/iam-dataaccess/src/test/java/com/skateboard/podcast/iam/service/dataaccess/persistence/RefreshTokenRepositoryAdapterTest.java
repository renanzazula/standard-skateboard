package com.skateboard.podcast.iam.service.dataaccess.persistence;

import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository.RefreshTokenRecord;
import com.skateboard.podcast.iam.service.dataaccess.IamDataaccessTestConfig;
import com.skateboard.podcast.iam.service.dataaccess.persistence.jpa.SpringDataRefreshTokenRepository;
import jakarta.persistence.EntityManager;
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

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = IamDataaccessTestConfig.class)
@Import(RefreshTokenRepositoryAdapter.class)
class RefreshTokenRepositoryAdapterTest {

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
    private RefreshTokenRepositoryAdapter adapter;

    @Autowired
    private SpringDataRefreshTokenRepository repo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindByTokenHash() {
        final UUID tokenId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID familyId = UUID.randomUUID();
        final RefreshTokenRecord record = new RefreshTokenRecord(
                tokenId,
                userId,
                "token-hash",
                familyId,
                "device-1",
                "Pixel",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                null,
                null,
                null
        );

        adapter.save(record);

        final var found = adapter.findByTokenHash("token-hash");
        assertTrue(found.isPresent());
        assertEquals(tokenId, found.get().id());
    }

    @Test
    void revokeById_marksToken() {
        final UUID tokenId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final RefreshTokenRecord record = new RefreshTokenRecord(
                tokenId,
                userId,
                "revokable-hash",
                UUID.randomUUID(),
                "device-2",
                "Phone",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                null,
                null,
                null
        );
        adapter.save(record);
        repo.flush();

        final Instant revokedAt = Instant.now();
        adapter.revokeById(tokenId, revokedAt);
        repo.flush();

        entityManager.clear();
        final var entity = repo.findById(tokenId).orElseThrow();
        assertNotNull(entity.getRevokedAt());
    }

    @Test
    void revokeByFamilyAndUser_updatesMatchingTokens() {
        final UUID userId = UUID.randomUUID();
        final UUID familyId = UUID.randomUUID();

        final RefreshTokenRecord first = new RefreshTokenRecord(
                UUID.randomUUID(),
                userId,
                "family-1",
                familyId,
                "device-a",
                "Laptop",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                null,
                null,
                null
        );
        final RefreshTokenRecord second = new RefreshTokenRecord(
                UUID.randomUUID(),
                userId,
                "family-2",
                familyId,
                "device-b",
                "Tablet",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                null,
                null,
                null
        );
        final RefreshTokenRecord otherFamily = new RefreshTokenRecord(
                UUID.randomUUID(),
                userId,
                "family-3",
                UUID.randomUUID(),
                "device-c",
                "Phone",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                null,
                null,
                null
        );

        adapter.save(first);
        adapter.save(second);
        adapter.save(otherFamily);
        repo.flush();

        final Instant revokedAtFamily = Instant.now();
        adapter.revokeByFamilyId(familyId, revokedAtFamily);
        repo.flush();
        entityManager.clear();

        assertNotNull(repo.findById(first.id()).orElseThrow().getRevokedAt());
        assertNotNull(repo.findById(second.id()).orElseThrow().getRevokedAt());
        assertNull(repo.findById(otherFamily.id()).orElseThrow().getRevokedAt());

        final Instant revokedAtUser = Instant.now();
        adapter.revokeByUserId(userId, revokedAtUser);
        repo.flush();
        entityManager.clear();

        assertNotNull(repo.findById(otherFamily.id()).orElseThrow().getRevokedAt());
    }
}
