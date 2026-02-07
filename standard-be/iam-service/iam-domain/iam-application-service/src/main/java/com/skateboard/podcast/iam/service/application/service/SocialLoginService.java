package com.skateboard.podcast.iam.service.application.service;

import com.skateboard.podcast.domain.exception.UnauthorizedException;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserId;
import com.skateboard.podcast.domain.valueobject.UserStatus;
import com.skateboard.podcast.iam.service.application.dto.AuthResult;
import com.skateboard.podcast.iam.service.application.port.in.SocialLoginUseCase;
import com.skateboard.podcast.iam.service.application.port.out.RefreshTokenRepository;
import com.skateboard.podcast.iam.service.application.port.out.TokenProvider;
import com.skateboard.podcast.iam.service.application.port.out.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

public class SocialLoginService implements SocialLoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    public SocialLoginService(
            final UserRepository userRepository,
            final RefreshTokenRepository refreshTokenRepository,
            final TokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResult login(
            final Provider provider,
            final String token,
            final String deviceId,
            final String deviceName
    ) {
        if (provider == null) {
            throw new ValidationException("provider cannot be blank");
        }
        if (provider == Provider.MANUAL || provider == Provider.PASSCODE) {
            throw new ValidationException("provider not supported for social login");
        }
        if (token == null || token.isBlank()) {
            throw new ValidationException("token cannot be blank");
        }
        if (deviceId == null || deviceId.isBlank()) {
            throw new ValidationException("deviceId cannot be blank");
        }

        final Email email = resolveEmail(provider, token);
        final var user = userRepository.findByEmail(email)
                .orElseGet(() -> createSocialUser(email, provider));

        if (user.status() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("user not active");
        }
        if (user.provider() != provider) {
            throw new UnauthorizedException("provider mismatch");
        }

        return issueTokens(user, deviceId, deviceName);
    }

    private Email resolveEmail(final Provider provider, final String token) {
        try {
            return Email.of(token);
        } catch (final ValidationException ignored) {
            final String uuid = UUID.nameUUIDFromBytes((provider.name() + ":" + token)
                    .getBytes(StandardCharsets.UTF_8))
                    .toString();
            return Email.of(provider.name().toLowerCase() + "-" + uuid + "@social.local");
        }
    }

    private UserRepository.UserRecord createSocialUser(final Email email, final Provider provider) {
        final var user = new UserRepository.UserRecord(
                UUID.randomUUID(),
                email,
                null,
                Role.USER,
                provider,
                UserStatus.ACTIVE
        );
        return userRepository.save(user);
    }

    private AuthResult issueTokens(
            final UserRepository.UserRecord user,
            final String deviceId,
            final String deviceName
    ) {
        final String access = tokenProvider.createAccessToken(
                user.id(),
                user.role().name(),
                user.email().value()
        );

        final String rawRefresh = tokenProvider.newRefreshToken();
        final String refreshHash = tokenProvider.hashRefreshToken(rawRefresh);

        final Instant now = Instant.now();
        final Instant refreshExp = now.plusSeconds(tokenProvider.refreshTtlSeconds());
        final UUID familyId = UUID.randomUUID();

        refreshTokenRepository.save(new RefreshTokenRepository.RefreshTokenRecord(
                UUID.randomUUID(),
                user.id(),
                refreshHash,
                familyId,
                deviceId,
                deviceName,
                now,
                refreshExp,
                null,
                null,
                null
        ));

        return new AuthResult(
                access,
                tokenProvider.accessTtlSeconds(),
                rawRefresh,
                tokenProvider.refreshTtlSeconds(),
                UserId.of(user.id()),
                user.email(),
                user.role(),
                user.provider()
        );
    }
}
