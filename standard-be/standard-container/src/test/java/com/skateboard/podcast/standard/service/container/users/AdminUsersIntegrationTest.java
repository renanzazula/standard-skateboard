package com.skateboard.podcast.standard.service.container.users;

import com.skateboard.podcast.standardbe.api.model.AdminPasscodeLoginRequest;
import com.skateboard.podcast.standardbe.api.model.AdminUser;
import com.skateboard.podcast.standardbe.api.model.AdminUserStatusUpdateRequest;
import com.skateboard.podcast.standardbe.api.model.AdminUserUpdateRequest;
import com.skateboard.podcast.standardbe.api.model.AuthResponse;
import com.skateboard.podcast.standardbe.api.model.DeviceInfo;
import com.skateboard.podcast.standardbe.api.model.RegisterRequest;
import com.skateboard.podcast.standardbe.api.model.Role;
import com.skateboard.podcast.standardbe.api.model.UserStatus;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminUsersIntegrationTest {

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
    void adminUsersRestFlow_updatesStatusAndDeletes() {
        final String email = "user-" + UUID.randomUUID() + "@example.com";
        final AuthResponse userAuth = registerUser(email);
        final UUID userId = UUID.fromString(userAuth.getUser().getId().toString());

        final String adminToken = adminToken();

        final ResponseEntity<AdminUser[]> listResponse = restTemplate.exchange(
            url("/admin/users"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(adminToken)),
            AdminUser[].class
        );
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        final List<AdminUser> users = Arrays.asList(listResponse.getBody());
        assertTrue(users.stream().anyMatch(user -> userId.equals(user.getId())));

        final AdminUserUpdateRequest updateRequest = new AdminUserUpdateRequest()
                .name("Updated User")
                .username("updated-" + UUID.randomUUID())
                .role(Role.ADMIN);

        final ResponseEntity<AdminUser> updated = restTemplate.exchange(
                url("/admin/users/" + userId),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, authHeaders(adminToken)),
                AdminUser.class
        );
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertNotNull(updated.getBody());
        assertEquals(Role.ADMIN, updated.getBody().getRole());
        assertEquals(updateRequest.getName(), updated.getBody().getName());

        final AdminUserStatusUpdateRequest statusRequest = new AdminUserStatusUpdateRequest()
                .status(UserStatus.DISABLED);

        final ResponseEntity<AdminUser> statusUpdated = restTemplate.exchange(
                url("/admin/users/" + userId + "/status"),
                HttpMethod.PATCH,
                new HttpEntity<>(statusRequest, authHeaders(adminToken)),
                AdminUser.class
        );
        assertEquals(HttpStatus.OK, statusUpdated.getStatusCode());
        assertNotNull(statusUpdated.getBody());
        assertEquals(UserStatus.DISABLED, statusUpdated.getBody().getStatus());

        final ResponseEntity<Void> deleted = restTemplate.exchange(
                url("/admin/users/" + userId),
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(adminToken)),
                Void.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleted.getStatusCode());

        final ResponseEntity<AdminUser[]> afterDelete = restTemplate.exchange(
                url("/admin/users"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(adminToken)),
                AdminUser[].class
        );
        assertEquals(HttpStatus.OK, afterDelete.getStatusCode());
        assertNotNull(afterDelete.getBody());
        final List<AdminUser> afterUsers = Arrays.asList(afterDelete.getBody());
        assertTrue(afterUsers.stream().noneMatch(user -> userId.equals(user.getId())));
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
