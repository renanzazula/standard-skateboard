package com.skateboard.podcast.iam.service.application.adapter.in.rest;

import com.skateboard.podcast.domain.security.CurrentUser;
import com.skateboard.podcast.iam.service.application.port.in.LoginUseCase;
import com.skateboard.podcast.iam.service.application.port.in.LogoutUseCase;
import com.skateboard.podcast.iam.service.application.port.in.RefreshUseCase;
import com.skateboard.podcast.iam.service.application.port.in.RegisterUseCase;
import com.skateboard.podcast.standardbe.api.PublicAuthApi;
import com.skateboard.podcast.standardbe.api.model.AuthResponse;
import com.skateboard.podcast.standardbe.api.model.LoginRequest;
import com.skateboard.podcast.standardbe.api.model.RefreshRequest;
import com.skateboard.podcast.standardbe.api.model.RegisterRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class PublicAuthController implements PublicAuthApi {

    private final RegisterUseCase registerService;
    private final LoginUseCase loginService;
    private final RefreshUseCase refreshService;
    private final LogoutUseCase logoutService;
    private final AuthMapper authMapper;

    public PublicAuthController(
            final RegisterUseCase registerService,
            final LoginUseCase loginService,
            final RefreshUseCase refreshService,
            final LogoutUseCase logoutService,
            final AuthMapper authMapper
    ) {
        this.registerService = registerService;
        this.loginService = loginService;
        this.refreshService = refreshService;
        this.logoutService = logoutService;
        this.authMapper = authMapper;
    }

    @Override
    @PostMapping(
            value = PublicAuthApi.PATH_PUBLIC_AUTH_REGISTER,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthResponse> publicAuthRegister(final RegisterRequest registerRequest) {
        final var result = registerService.register(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getDevice().getDeviceId(),
                registerRequest.getDevice().getDeviceName()
        );
        return ResponseEntity.ok(authMapper.toAuthResponse(result));
    }

    @Override
    @PostMapping(
            value = PublicAuthApi.PATH_PUBLIC_AUTH_LOGIN,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthResponse> publicAuthLogin(final LoginRequest loginRequest) {
        final var result = loginService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                loginRequest.getDevice().getDeviceId(),
                loginRequest.getDevice().getDeviceName()
        );
        return ResponseEntity.ok(authMapper.toAuthResponse(result));
    }

    @Override
    @PostMapping(
            value = PublicAuthApi.PATH_PUBLIC_AUTH_REFRESH,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthResponse> publicAuthRefresh(final RefreshRequest refreshRequest) {
        final var result = refreshService.refresh(
                refreshRequest.getRefreshToken(),
                refreshRequest.getDeviceId()
        );
        return ResponseEntity.ok(authMapper.toAuthResponse(result));
    }

    @Override
    @PostMapping(value = PublicAuthApi.PATH_PUBLIC_AUTH_LOGOUT)
    public ResponseEntity<Void> publicAuthLogout() {
        logoutService.logoutAllForUser(requireUserId());
        return ResponseEntity.noContent().build();
    }

    private static UUID requireUserId() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }
        if (auth.getPrincipal() instanceof final CurrentUser currentUser) {
            return currentUser.userId();
        }
        throw new IllegalStateException("No authenticated user");
    }
}
