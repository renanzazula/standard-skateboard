package com.skateboard.podcast.standard.service.container.navigationconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.standardbe.api.model.AuthResponse;
import com.skateboard.podcast.standardbe.api.model.DeviceInfo;
import com.skateboard.podcast.standardbe.api.model.LoginRequest;
import com.skateboard.podcast.standardbe.api.model.NavigationConfig;
import com.skateboard.podcast.standardbe.api.model.NavigationTab;
import com.skateboard.podcast.standardbe.api.model.Provider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClientResponseException;
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
class NavigationConfigIntegrationTest {

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
    void navigationConfigRestFlow_updatesAndReads() {
        final ResponseEntity<NavigationConfig> guestInitial = restTemplate.getForEntity(
                url("/public/navigation-config"),
                NavigationConfig.class
        );
        assertEquals(HttpStatus.OK, guestInitial.getStatusCode());
        assertNotNull(guestInitial.getBody());
        assertNotNull(guestInitial.getBody().getTabs());
        assertEquals(0, guestInitial.getBody().getTabs().size());

        final String token = adminToken();
        final ResponseEntity<NavigationConfig> initial = restTemplate.exchange(
                url("/public/navigation-config"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                NavigationConfig.class
        );
        assertEquals(HttpStatus.OK, initial.getStatusCode());
        assertNotNull(initial.getBody());
        assertNotNull(initial.getBody().getTabs());
        assertTrue(initial.getBody().getTabs().stream()
                .anyMatch(tab -> "settings".equals(tab.getId()) && Boolean.TRUE.equals(tab.getEnabled())));
        final NavigationConfig updatedConfig = sampleConfig();

        final ResponseEntity<NavigationConfig> updated = restTemplate.exchange(
                url("/admin/navigation-config"),
                HttpMethod.PUT,
                new HttpEntity<>(updatedConfig, authHeaders(token)),
                NavigationConfig.class
        );
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertNotNull(updated.getBody());
        assertTabsMatch(sampleConfig().getTabs(), updated.getBody().getTabs());

        final ResponseEntity<NavigationConfig> after = restTemplate.exchange(
                url("/public/navigation-config"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                NavigationConfig.class
        );
        assertEquals(HttpStatus.OK, after.getStatusCode());
        assertNotNull(after.getBody());
        assertTabsMatch(sampleConfig().getTabs(), after.getBody().getTabs());
    }

    @Test
    void navigationConfigUpdate_emitsWebSocketEvent() throws Exception {
        final String token = adminToken();
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
            final ResponseEntity<NavigationConfig> updated = restTemplate.exchange(
                    url("/admin/navigation-config"),
                    HttpMethod.PUT,
                    new HttpEntity<>(sampleConfig(), authHeaders(token)),
                    NavigationConfig.class
            );
            assertEquals(HttpStatus.OK, updated.getStatusCode());

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            final String payload = messages.poll(1, TimeUnit.SECONDS);
            assertNotNull(payload);

            final JsonNode json = objectMapper.readTree(payload);
            assertEquals("navigation.updated", json.get("type").asText());
            assertTrue(json.hasNonNull("timestamp"));
            assertTrue(json.hasNonNull("version"));
            assertTrue(json.hasNonNull("payload"));
            assertTrue(json.get("payload").hasNonNull("updatedAt"));
        } finally {
            session.close();
        }
    }

    @Test
    void navigationConfigUpdate_rejectsDisabledSettingsTab() {
        final NavigationConfig invalid = sampleConfig();
        invalid.getTabs().stream()
                .filter(tab -> "settings".equals(tab.getId()))
                .findFirst()
                .ifPresent(tab -> tab.enabled(false));

        assertBadRequest(invalid);
    }

    @Test
    void navigationConfigUpdate_rejectsDuplicateIds() {
        final NavigationConfig invalid = sampleConfig();
        invalid.getTabs().get(2).id("settings");

        assertBadRequest(invalid);
    }

    @Test
    void navigationConfigUpdate_rejectsBlankName() {
        final NavigationConfig invalid = sampleConfig();
        invalid.getTabs().get(1).name(" ");

        assertBadRequest(invalid);
    }

    @Test
    void navigationConfigUpdate_rejectsDisabledIndexTab() {
        final NavigationConfig invalid = sampleConfig();
        invalid.getTabs().stream()
                .filter(tab -> "index".equals(tab.getId()))
                .findFirst()
                .ifPresent(tab -> tab.enabled(false));

        assertBadRequest(invalid);
    }

    @Test
    void navigationConfigUpdate_rejectsMissingIndexTab() {
        final NavigationConfig invalid = new NavigationConfig()
                .tabs(sampleConfig().getTabs().stream()
                        .filter(tab -> !"index".equals(tab.getId()))
                        .toList());

        assertBadRequest(invalid);
    }

    @Test
    void navigationConfigUpdate_rejectsSettingsNotSystem() {
        final NavigationConfig invalid = sampleConfig();
        invalid.getTabs().stream()
                .filter(tab -> "settings".equals(tab.getId()))
                .findFirst()
                .ifPresent(tab -> tab.isSystem(false));

        assertBadRequest(invalid);
    }

    private NavigationConfig sampleConfig() {
        final NavigationTab events = new NavigationTab()
                .id("index")
                .name("Events")
                .icon("Calendar")
                .order(0)
                .enabled(true)
                .isSystem(true);
        final NavigationTab settings = new NavigationTab()
                .id("settings")
                .name("Admin")
                .icon("Settings")
                .order(1)
                .enabled(true)
                .isSystem(true);
        final NavigationTab community = new NavigationTab()
                .id("community")
                .name("Community")
                .icon("LayoutGrid")
                .order(2)
                .enabled(true)
                .isSystem(false);

        return new NavigationConfig().tabs(List.of(events, settings, community));
    }

    private void assertBadRequest(final NavigationConfig payload) {
        final String token = adminToken();
        try {
            final ResponseEntity<String> response = restTemplate.exchange(
                    url("/admin/navigation-config"),
                    HttpMethod.PUT,
                    new HttpEntity<>(payload, authHeaders(token)),
                    String.class
            );
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        } catch (final RestClientResponseException e) {
            assertEquals(HttpStatus.BAD_REQUEST.value(), e.getRawStatusCode());
        }
    }

    private static void assertTabsMatch(
            final List<NavigationTab> expected,
            final List<NavigationTab> actual
    ) {
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        for (final NavigationTab expectedTab : expected) {
            final NavigationTab actualTab = actual.stream()
                    .filter(tab -> expectedTab.getId().equals(tab.getId()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(actualTab);
            assertEquals(expectedTab.getName(), actualTab.getName());
            assertEquals(expectedTab.getIcon(), actualTab.getIcon());
            assertEquals(expectedTab.getOrder(), actualTab.getOrder());
            assertEquals(expectedTab.getEnabled(), actualTab.getEnabled());
            assertEquals(expectedTab.getIsSystem(), actualTab.getIsSystem());
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
        final LoginRequest request = new LoginRequest()
                .provider(Provider.MANUAL)
                .email("admin@example.com")
                .password("admin123")
                .device(deviceInfo);

        final ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                url("/public/auth/login"),
                request,
                AuthResponse.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTokens());
        return response.getBody().getTokens().getAccessToken();
    }
}
