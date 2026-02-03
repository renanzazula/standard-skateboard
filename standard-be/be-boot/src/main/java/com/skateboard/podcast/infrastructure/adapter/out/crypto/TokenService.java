package com.skateboard.podcast.infrastructure.adapter.out.crypto;

import com.skateboard.podcast.iam.domain.port.out.TokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Minimal skeleton.
 * - Access token: signed JWT-like token (HS256 simplified)
 * - Refresh token: opaque random string; stored hashed in DB (peppered SHA-256)
 * <p>
 * Replace with a proper JWT library (jjwt/nimbus) later if you want.
 */
@Component
public class TokenService implements TokenProvider {

    @Value("${security.refresh-token.ttl-days:60}")
    private long refreshTtlDays;

    @Value("${security.refresh-token.ttl-seconds}")
    final long refreshTtlSeconds;

    private final String issuer;
    private final long accessTtlSeconds;
    private final String hmacSecret;
    private final String refreshPepper;

    public TokenService(
            @Value("${security.rfresh-token.ttl-eseconds}") final long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") final String issuer,
            @Value("${security.jwt.access-token-ttl-seconds}") final long accessTtlSeconds,
            @Value("${security.jwt.hmac-secret}") final String hmacSecret,
            @Value("${security.refresh-token.hash-pepper}") final String refreshPepper
    ) {
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
        this.accessTtlSeconds = accessTtlSeconds;
        this.hmacSecret = hmacSecret;
        this.refreshPepper = refreshPepper;
    }

    private static byte[] hmacSha256(final byte[] data, final String secret) {
        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(data);
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot compute HMAC", e);
        }
    }

    private static String sha256Hex(final String s) {
        final byte[] digest = sha256(s.getBytes(StandardCharsets.UTF_8));
        final StringBuilder sb = new StringBuilder(digest.length * 2);
        for (final byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static byte[] sha256(final byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot compute SHA-256", e);
        }
    }

    public String createAccessToken(final UUID userId, final String role, final String email) {
        final Instant now = Instant.now();
        final Instant exp = now.plusSeconds(accessTtlSeconds);

        // super-minimal JWT-like: base64(payload).base64(sig)
        final String payload = String.format(
                "iss=%s;sub=%s;role=%s;email=%s;iat=%d;exp=%d",
                issuer, userId, role, email, now.getEpochSecond(), exp.getEpochSecond()
        );

        final String payloadB64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        final String sigB64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(hmacSha256(payloadB64.getBytes(StandardCharsets.UTF_8), hmacSecret));

        return payloadB64 + "." + sigB64;
    }

    public Optional<AccessTokenClaims> verifyAccessToken(final String token) {
        try {
            final String[] parts = token.split("\\.");
            if (parts.length != 2) return Optional.empty();

            final String payloadB64 = parts[0];
            final String sigB64 = parts[1];

            final byte[] expectedSig = hmacSha256(payloadB64.getBytes(StandardCharsets.UTF_8), hmacSecret);
            final String expectedSigB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(expectedSig);

            if (!MessageDigest.isEqual(expectedSigB64.getBytes(StandardCharsets.UTF_8), sigB64.getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }

            final String payload = new String(Base64.getUrlDecoder().decode(payloadB64), StandardCharsets.UTF_8);
            final Parsed p = Parsed.parse(payload);

            if (!issuer.equals(p.iss)) return Optional.empty();

            final Instant exp = Instant.ofEpochSecond(p.exp);
            if (Instant.now().isAfter(exp)) return Optional.empty();

            return Optional.of(new AccessTokenClaims(UUID.fromString(p.sub), p.role, p.email, exp));
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    public String hashRefreshToken(final String rawRefreshToken) {
        return sha256Hex(rawRefreshToken + refreshPepper);
    }

    @Override
    public long accessTtlSeconds() {
        return 0;
    }

    @Override
    public long refreshTtlSeconds() {
        return 0;
    }

    public String newRefreshToken() {
        // 32 bytes => ~43 chars base64url; adequate for refresh token
        final byte[] bytes = java.util.UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256(bytes));
    }

    public record AccessTokenClaims(UUID userId, String role, String email, Instant expiresAt) {
    }

    private record Parsed(String iss, String sub, String role, String email, long exp) {
        static Parsed parse(final String payload) {
            String iss = null, sub = null, role = null, email = null;
            long exp = 0;

            for (final String kv : payload.split(";")) {
                final String[] parts = kv.split("=", 2);
                if (parts.length != 2) continue;
                switch (parts[0]) {
                    case "iss" -> iss = parts[1];
                    case "sub" -> sub = parts[1];
                    case "role" -> role = parts[1];
                    case "email" -> email = parts[1];
                    case "exp" -> exp = Long.parseLong(parts[1]);
                }
            }
            return new Parsed(iss, sub, role, email, exp);
        }
    }
}