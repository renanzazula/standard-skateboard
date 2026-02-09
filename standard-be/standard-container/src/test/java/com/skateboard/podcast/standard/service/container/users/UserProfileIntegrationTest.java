package com.skateboard.podcast.standard.service.container.users;

import com.skateboard.podcast.standardbe.api.model.AuthResponse;
import com.skateboard.podcast.standardbe.api.model.DeviceInfo;
import com.skateboard.podcast.standardbe.api.model.RegisterRequest;
import com.skateboard.podcast.standardbe.api.model.UserProfile;
import com.skateboard.podcast.standardbe.api.model.UserProfileUpdateRequest;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserProfileIntegrationTest {

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

    @Test
    void userProfileRestFlow_updatesAndReads() {
        final String email = "user-" + UUID.randomUUID() + "@example.com";
        final AuthResponse auth = registerUser(email);
        final String token = auth.getTokens().getAccessToken();

        final ResponseEntity<UserProfile> initial = restTemplate.exchange(
                url("/users/me"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                UserProfile.class
        );
        assertEquals(HttpStatus.OK, initial.getStatusCode());
        assertNotNull(initial.getBody());
        assertEquals(email, initial.getBody().getEmail());

        final UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest()
                .name("Skate Admin")
                .username("skate-" + UUID.randomUUID())
                .avatarUrl(URI.create("https://cdn.example.com/avatar.png"));

        final ResponseEntity<UserProfile> updated = restTemplate.exchange(
                url("/users/me"),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, authHeaders(token)),
                UserProfile.class
        );
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertNotNull(updated.getBody());
        assertEquals(updateRequest.getName(), updated.getBody().getName());
        assertEquals(updateRequest.getUsername(), updated.getBody().getUsername());
        assertEquals(updateRequest.getAvatarUrl(), updated.getBody().getAvatarUrl());

        final ResponseEntity<UserProfile> after = restTemplate.exchange(
                url("/users/me"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                UserProfile.class
        );
        assertEquals(HttpStatus.OK, after.getStatusCode());
        assertNotNull(after.getBody());
        assertEquals(updateRequest.getName(), after.getBody().getName());
    }

    private String url(final String path) {
        return "http://localhost:" + port + "/api/v1" + path;
    }

    private HttpHeaders authHeaders(final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    private AuthResponse registerUser(final String email) {
        final DeviceInfo deviceInfo = new DeviceInfo()
                .deviceId("test-device")
                .deviceName("integration")
                .platform(DeviceInfo.PlatformEnum.WEB);
        final RegisterRequest request = new RegisterRequest()
                .email(email)
                .password("secret-password")
                .device(deviceInfo);

        final ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                url("/public/auth/register"),
                request,
                AuthResponse.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTokens());
        return response.getBody();
    }
}
