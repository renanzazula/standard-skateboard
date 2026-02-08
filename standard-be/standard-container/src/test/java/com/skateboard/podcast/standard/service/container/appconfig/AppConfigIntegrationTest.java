package com.skateboard.podcast.standard.service.container.appconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.standardbe.api.model.AdminPasscodeLoginRequest;
import com.skateboard.podcast.standardbe.api.model.AppConfig;
import com.skateboard.podcast.standardbe.api.model.AuthResponse;
import com.skateboard.podcast.standardbe.api.model.DeviceInfo;
import com.skateboard.podcast.standardbe.api.model.SplashConfig;
import com.skateboard.podcast.standardbe.api.model.SplashMediaType;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppConfigIntegrationTest {

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
    void appConfigRestFlow_updatesAndReads() {
        final ResponseEntity<AppConfig> initial = restTemplate.getForEntity(
                url("/public/app-config"),
                AppConfig.class
        );
        assertEquals(HttpStatus.OK, initial.getStatusCode());
        assertNotNull(initial.getBody());
        assertEquals(Boolean.FALSE, initial.getBody().getSocialLoginEnabled());
        assertEquals(20, initial.getBody().getPostsPerPage());
        assertNotNull(initial.getBody().getSplash());

        final String token = adminToken();
        final AppConfig updatedConfig = sampleConfig(true, 25);

        final ResponseEntity<AppConfig> updated = restTemplate.exchange(
                url("/admin/app-config"),
                HttpMethod.PUT,
                new HttpEntity<>(updatedConfig, authHeaders(token)),
                AppConfig.class
        );
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertNotNull(updated.getBody());

        final ResponseEntity<AppConfig> after = restTemplate.getForEntity(
                url("/public/app-config"),
                AppConfig.class
        );
        assertEquals(HttpStatus.OK, after.getStatusCode());
        assertNotNull(after.getBody());
        assertEquals(Boolean.TRUE, after.getBody().getSocialLoginEnabled());
        assertEquals(25, after.getBody().getPostsPerPage());
    }

    @Test
    void appConfigUpdate_emitsWebSocketEvent() throws Exception {
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
            final ResponseEntity<AppConfig> updated = restTemplate.exchange(
                    url("/admin/app-config"),
                    HttpMethod.PUT,
                    new HttpEntity<>(sampleConfig(true, 30), authHeaders(token)),
                    AppConfig.class
            );
            assertEquals(HttpStatus.OK, updated.getStatusCode());

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            final String payload = messages.poll(1, TimeUnit.SECONDS);
            assertNotNull(payload);

            final JsonNode json = objectMapper.readTree(payload);
            assertEquals("config.updated", json.get("type").asText());
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

    private AppConfig sampleConfig(final boolean socialLoginEnabled, final int postsPerPage) {
        final SplashConfig splash = new SplashConfig()
                .enabled(true)
                .mediaType(SplashMediaType.IMAGE)
                .mediaUrl("https://cdn.example.com/splash.jpg")
                .duration(5000)
                .showCloseButton(true)
                .closeButtonDelay(2000);

        return new AppConfig()
                .socialLoginEnabled(socialLoginEnabled)
                .postsPerPage(postsPerPage)
                .splash(splash);
    }
}
