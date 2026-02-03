package com.skateboard.podcast.infrastructure.adapter.in.rest.publicapi;

import com.skateboard.podcast.iam.application.dto.AuthResult;
import com.skateboard.podcast.iam.application.service.LoginService;
import com.skateboard.podcast.iam.application.service.RegisterService;
import com.skateboard.podcast.standardbe.api.PublicApi;
import com.skateboard.podcast.standardbe.api.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthControllerImpl implements PublicApi {

    private final RegisterService registerService;
    private final LoginService loginService;

    public AuthControllerImpl(final RegisterService registerService, final LoginService loginService) {
        this.registerService = registerService;
        this.loginService = loginService;
    }

    private static AuthResponse toAuthResponse(final AuthResult r) {
        final AuthTokens tokens = new AuthTokens()
                .accessToken(r.accessToken())
                .expiresInSeconds((int) r.expiresInSeconds())
                .refreshToken(r.refreshToken())
                .refreshExpiresInSeconds((int) r.refreshExpiresInSeconds());

        final UserSummary user = new UserSummary()
                .id(r.userId())
                .email(r.email())
                .role(Role.valueOf(r.role()))
                .provider(Provider.valueOf(r.provider()));

        return new AuthResponse()
                .tokens(tokens)
                .user(user);
    }

    @Override
    public ResponseEntity<AuthResponse> publicAuthRegister(final RegisterRequest registerRequest) {
        final AuthResult result = registerService.register(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getDevice().getDeviceId(),
                registerRequest.getDevice().getDeviceName()
        );
        return ResponseEntity.ok(toAuthResponse(result));
    }

    @Override
    public ResponseEntity<AuthResponse> publicAuthLogin(final LoginRequest loginRequest) {
        final AuthResult result = loginService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                loginRequest.getDevice().getDeviceId(),
                loginRequest.getDevice().getDeviceName()
        );
        return ResponseEntity.ok(toAuthResponse(result));
    }

    // refresh/logout will be implemented next (RefreshService/LogoutService)
    @Override
    public ResponseEntity<AuthResponse> publicAuthRefresh(final RefreshRequest refreshRequest) {
        return ResponseEntity.status(501).build();
    }

    @Override
    public ResponseEntity<Void> publicAuthLogout() {
        return ResponseEntity.noContent().build();
    }
}
