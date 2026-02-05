package com.skateboard.podcast.iam.service.dataaccess.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenServiceTest {

    @Test
    void returnsConfiguredTtls() {
        final TokenService service = new TokenService(
                3600,
                "issuer",
                900,
                "secretsecretsecretsecretsecret12",
                "pepper"
        );

        assertEquals(900, service.accessTtlSeconds());
        assertEquals(3600, service.refreshTtlSeconds());
    }
}
