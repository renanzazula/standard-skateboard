package com.skateboard.podcast.standard.service.container.settingsconfig;

import com.skateboard.podcast.standardbe.api.model.AdminPasscodeLoginRequest;
import com.skateboard.podcast.standardbe.api.model.AuthResponse;
import com.skateboard.podcast.standardbe.api.model.DeviceInfo;
import com.skateboard.podcast.standardbe.api.model.Language;
import com.skateboard.podcast.standardbe.api.model.LanguageConfig;
import com.skateboard.podcast.standardbe.api.model.ProfileConfig;
import com.skateboard.podcast.standardbe.api.model.ServiceMode;
import com.skateboard.podcast.standardbe.api.model.SessionConfig;
import com.skateboard.podcast.standardbe.api.model.SettingsAuthMethods;
import com.skateboard.podcast.standardbe.api.model.SettingsConfig;
import com.skateboard.podcast.standardbe.api.model.SettingsServiceModes;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SettingsConfigIntegrationTest {

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
    void settingsConfigRestFlow_updatesAndReads() {
        final ResponseEntity<SettingsConfig> initial = restTemplate.getForEntity(
                url("/public/settings-config"),
                SettingsConfig.class
        );
        assertEquals(HttpStatus.OK, initial.getStatusCode());
        assertNotNull(initial.getBody());
        assertNotNull(initial.getBody().getEnabledAuthMethods());
        assertEquals(Boolean.TRUE, initial.getBody().getEnabledAuthMethods().getManual());

        final String token = adminToken();
        final SettingsConfig updatedConfig = sampleConfig();

        final ResponseEntity<SettingsConfig> updated = restTemplate.exchange(
                url("/admin/settings-config"),
                HttpMethod.PUT,
                new HttpEntity<>(updatedConfig, authHeaders(token)),
                SettingsConfig.class
        );
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertNotNull(updated.getBody());
        assertEquals(Boolean.TRUE, updated.getBody().getEnabledAuthMethods().getGoogle());
        assertEquals(ServiceMode.REAL, updated.getBody().getServiceModes().getGoogle());

        final ResponseEntity<SettingsConfig> after = restTemplate.getForEntity(
                url("/public/settings-config"),
                SettingsConfig.class
        );
        assertEquals(HttpStatus.OK, after.getStatusCode());
        assertNotNull(after.getBody());
        assertEquals(3600000, after.getBody().getSessionConfig().getMaxTime());
        assertEquals("pt", after.getBody().getLanguageConfig().getDefaultLanguage().getCode());
    }

    private SettingsConfig sampleConfig() {
        final SettingsAuthMethods authMethods = new SettingsAuthMethods()
                .google(true)
                .apple(false)
                .manual(true);
        final SettingsServiceModes serviceModes = new SettingsServiceModes()
                .google(ServiceMode.REAL)
                .apple(ServiceMode.MOCK)
                .manual(ServiceMode.REAL);
        final SessionConfig sessionConfig = new SessionConfig()
                .maxTime(3600000)
                .idleTime(1800000)
                .autoRefresh(true);

        final Language english = new Language()
                .code("en")
                .name("English")
                .nativeName("English")
                .flag("EN");
        final Language portuguese = new Language()
                .code("pt")
                .name("Portuguese")
                .nativeName("Portugues")
                .flag("PT");
        final LanguageConfig languageConfig = new LanguageConfig()
                .availableLanguages(List.of(english, portuguese))
                .defaultLanguage(portuguese);

        final ProfileConfig profileConfig = new ProfileConfig()
                .usernameMinLength(3)
                .usernameMaxLength(25)
                .avatarMaxSizeMB(6)
                .allowedAvatarFormats(List.of("image/png", "image/jpeg"));

        return new SettingsConfig()
                .enabledAuthMethods(authMethods)
                .serviceModes(serviceModes)
                .sessionConfig(sessionConfig)
                .languageConfig(languageConfig)
                .profileConfig(profileConfig);
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
}
