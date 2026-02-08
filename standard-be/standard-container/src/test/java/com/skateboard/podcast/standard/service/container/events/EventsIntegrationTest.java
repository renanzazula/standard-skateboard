package com.skateboard.podcast.standard.service.container.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.standardbe.api.model.AdminPasscodeLoginRequest;
import com.skateboard.podcast.standardbe.api.model.AuthResponse;
import com.skateboard.podcast.standardbe.api.model.BlockType;
import com.skateboard.podcast.standardbe.api.model.CreateEventRequest;
import com.skateboard.podcast.standardbe.api.model.DeviceInfo;
import com.skateboard.podcast.standardbe.api.model.EventDetails;
import com.skateboard.podcast.standardbe.api.model.EventStatus;
import com.skateboard.podcast.standardbe.api.model.ImageRef;
import com.skateboard.podcast.standardbe.api.model.PageEventSummary;
import com.skateboard.podcast.standardbe.api.model.ParagraphBlock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventsIntegrationTest {

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

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void eventsRestFlow_publishesAndLists() {
        final String token = adminToken();
        final CreateEventRequest request = sampleEventRequest("street-jam-finals");

        final ResponseEntity<EventDetails> created = restTemplate.exchange(
                url("/admin/events"),
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                EventDetails.class
        );

        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertNotNull(created.getBody());

        final ResponseEntity<EventDetails> draftPublic = restTemplate.getForEntity(
                url("/public/events/" + created.getBody().getSlug()),
                EventDetails.class
        );
        assertEquals(HttpStatus.NOT_FOUND, draftPublic.getStatusCode());

        final ResponseEntity<EventDetails> published = restTemplate.exchange(
                url("/admin/events/" + created.getBody().getId() + "/publish"),
                HttpMethod.POST,
                new HttpEntity<>(authHeaders(token)),
                EventDetails.class
        );
        assertEquals(HttpStatus.OK, published.getStatusCode());
        assertNotNull(published.getBody());
        assertEquals(EventStatus.PUBLISHED, published.getBody().getStatus());

        final ResponseEntity<PageEventSummary> list = restTemplate.getForEntity(
                url("/public/events?page=0&size=10"),
                PageEventSummary.class
        );
        assertEquals(HttpStatus.OK, list.getStatusCode());
        assertNotNull(list.getHeaders().getETag());
        assertNotNull(list.getBody());
        assertTrue(list.getBody().getItems().stream()
                .anyMatch(item -> "street-jam-finals".equals(item.getSlug())));

        final ResponseEntity<EventDetails> publicEvent = restTemplate.getForEntity(
                url("/public/events/" + created.getBody().getSlug()),
                EventDetails.class
        );
        assertEquals(HttpStatus.OK, publicEvent.getStatusCode());
        assertNotNull(publicEvent.getBody());
        assertEquals("street-jam-finals", publicEvent.getBody().getSlug());
    }

    @Test
    void eventsWebSocketFlow_receivesPublishEvent() throws Exception {
        final String token = adminToken();
        final ResponseEntity<EventDetails> created = restTemplate.exchange(
                url("/admin/events"),
                HttpMethod.POST,
                new HttpEntity<>(sampleEventRequest("ws-street-jam"), authHeaders(token)),
                EventDetails.class
        );
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertNotNull(created.getBody());

        final BlockingQueue<String> messages = new ArrayBlockingQueue<>(1);
        final CountDownLatch latch = new CountDownLatch(1);

        final WebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(final WebSocketSession session, final TextMessage message) {
                messages.offer(message.getPayload());
                latch.countDown();
            }
        };

        final StandardWebSocketClient client = new StandardWebSocketClient();
        final WebSocketSession session = client
                .doHandshake(handler, new WebSocketHttpHeaders(), URI.create(wsUrl("/ws/events")))
                .get(5, TimeUnit.SECONDS);

        try {
            final ResponseEntity<EventDetails> published = restTemplate.exchange(
                    url("/admin/events/" + created.getBody().getId() + "/publish"),
                    HttpMethod.POST,
                    new HttpEntity<>(authHeaders(token)),
                    EventDetails.class
            );
            assertEquals(HttpStatus.OK, published.getStatusCode());

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            final String payload = messages.poll(1, TimeUnit.SECONDS);
            assertNotNull(payload);

            final JsonNode json = objectMapper.readTree(payload);
            assertEquals("event.published", json.get("type").asText());
            assertEquals(created.getBody().getId().toString(), json.get("payload").get("eventId").asText());
            assertEquals(created.getBody().getSlug(), json.get("payload").get("slug").asText());
        } finally {
            session.close();
        }
    }

    private String url(final String path) {
        return "http://localhost:" + port + "/api/v1" + path;
    }

    private String wsUrl(final String path) {
        return "ws://localhost:" + port + "/api/v1" + path;
    }

    private HttpHeaders authHeaders(final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    private String adminToken() {
        final DeviceInfo deviceInfo = new DeviceInfo()
                .deviceId("test-device")
                .deviceName("integration")
                .platform(DeviceInfo.PlatformEnum.WEB);
        final AdminPasscodeLoginRequest request = new AdminPasscodeLoginRequest()
                .passcode("admin123")
                .device(deviceInfo);

        final ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                url("/public/auth/admin-passcode"),
                request,
                AuthResponse.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTokens());
        return response.getBody().getTokens().getAccessToken();
    }

    private CreateEventRequest sampleEventRequest(final String slug) {
        final ParagraphBlock block = new ParagraphBlock();
        block.setType(BlockType.PARAGRAPH);
        block.setText("Welcome to the finals.");

        return new CreateEventRequest()
                .title("Street Jam Finals")
                .slug(slug)
                .excerpt("Finals with the top skaters.")
                .tags(List.of("street", "finals"))
                .thumbnail(new ImageRef().url("https://cdn.example.com/cover.jpg").alt("Event cover"))
                .content(List.of(block))
                .startAt(OffsetDateTime.parse("2026-05-12T19:30:00Z"))
                .endAt(OffsetDateTime.parse("2026-05-12T22:30:00Z"))
                .timezone("UTC")
                .location("Plaza Skatepark, Barcelona")
                .ticketsUrl("https://tickets.example.com/event");
    }
}
