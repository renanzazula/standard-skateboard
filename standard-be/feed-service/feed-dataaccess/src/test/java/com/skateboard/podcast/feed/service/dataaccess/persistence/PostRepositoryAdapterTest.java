package com.skateboard.podcast.feed.service.dataaccess.persistence;

import com.skateboard.podcast.domain.valueobject.PostStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;
import com.skateboard.podcast.feed.service.application.port.out.PostRepository.PostRecord;
import com.skateboard.podcast.feed.service.dataaccess.FeedDataaccessTestConfig;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = FeedDataaccessTestConfig.class)
@Import(PostRepositoryAdapter.class)
class PostRepositoryAdapterTest {

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
    private PostRepositoryAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindBySlugAndId() {
        final UUID id = UUID.randomUUID();
        final PostRecord record = new PostRecord(
                id,
                "Hello World",
                Slug.of("hello-world"),
                "Excerpt",
                List.of(Tag.of("news")),
                PostStatus.DRAFT,
                "{\"url\":\"https://example.com/thumb.png\",\"alt\":\"thumb\"}",
                "[{\"type\":\"paragraph\",\"text\":\"Hello\"}]",
                UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );

        adapter.save(record);

        final var bySlug = adapter.findBySlug(Slug.of("hello-world"));
        assertTrue(bySlug.isPresent());
        assertEquals(id, bySlug.get().id());

        final var byId = adapter.findById(id);
        assertTrue(byId.isPresent());
        assertEquals("Hello World", byId.get().title());
    }

    @Test
    void findPublished_returnsOnlyPublishedOrdered() {
        final PostRecord draft = new PostRecord(
                UUID.randomUUID(),
                "Draft",
                Slug.of("draft"),
                "Draft excerpt",
                List.of(Tag.of("draft")),
                PostStatus.DRAFT,
                null,
                "[{\"type\":\"paragraph\",\"text\":\"Draft\"}]",
                UUID.randomUUID(),
                Instant.parse("2024-01-02T00:00:00Z"),
                Instant.parse("2024-01-02T00:00:00Z"),
                null
        );
        final PostRecord older = new PostRecord(
                UUID.randomUUID(),
                "Older",
                Slug.of("older"),
                "Older excerpt",
                List.of(Tag.of("news")),
                PostStatus.PUBLISHED,
                null,
                "[{\"type\":\"paragraph\",\"text\":\"Older\"}]",
                UUID.randomUUID(),
                Instant.parse("2024-01-03T00:00:00Z"),
                Instant.parse("2024-01-03T00:00:00Z"),
                Instant.parse("2024-01-03T00:00:00Z")
        );
        final PostRecord newer = new PostRecord(
                UUID.randomUUID(),
                "Newer",
                Slug.of("newer"),
                "Newer excerpt",
                List.of(Tag.of("news")),
                PostStatus.PUBLISHED,
                null,
                "[{\"type\":\"paragraph\",\"text\":\"Newer\"}]",
                UUID.randomUUID(),
                Instant.parse("2024-01-04T00:00:00Z"),
                Instant.parse("2024-01-04T00:00:00Z"),
                Instant.parse("2024-01-04T00:00:00Z")
        );

        adapter.save(draft);
        adapter.save(older);
        adapter.save(newer);

        entityManager.flush();
        entityManager.clear();
        final var published = adapter.findPublished(0, 10);

        assertEquals(2, published.size());
        assertEquals("newer", published.get(0).slug().value());
        assertEquals("older", published.get(1).slug().value());
    }
}
