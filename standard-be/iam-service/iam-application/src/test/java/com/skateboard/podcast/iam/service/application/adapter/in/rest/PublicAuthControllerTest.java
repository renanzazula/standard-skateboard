package com.skateboard.podcast.iam.service.application.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.domain.security.CurrentUser;
import com.skateboard.podcast.domain.valueobject.Email;
import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.domain.valueobject.Role;
import com.skateboard.podcast.domain.valueobject.UserId;
import com.skateboard.podcast.iam.service.application.dto.AuthResult;
import com.skateboard.podcast.iam.service.application.port.in.LoginUseCase;
import com.skateboard.podcast.iam.service.application.port.in.LogoutUseCase;
import com.skateboard.podcast.iam.service.application.port.in.RefreshUseCase;
import com.skateboard.podcast.iam.service.application.port.in.RegisterUseCase;
import com.skateboard.podcast.standardbe.api.model.DeviceInfo;
import com.skateboard.podcast.standardbe.api.model.LoginRequest;
import com.skateboard.podcast.standardbe.api.model.RefreshRequest;
import com.skateboard.podcast.standardbe.api.model.RegisterRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthMapper.class)
class PublicAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private RegisterUseCase registerService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private LoginUseCase loginService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private RefreshUseCase refreshService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private LogoutUseCase logoutService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publicAuthRegister_returnsAuthResponse() throws Exception {
        final DeviceInfo device = new DeviceInfo()
                .deviceId("device-1")
                .deviceName("Pixel")
                .platform(DeviceInfo.PlatformEnum.WEB);
        final RegisterRequest request = new RegisterRequest()
                .email("user@example.com")
                .password("secret123")
                .device(device);

        final UUID userId = UUID.randomUUID();
        final AuthResult result = new AuthResult(
                "access-token",
                900,
                "refresh-token",
                5184000,
                UserId.of(userId),
                Email.of("user@example.com"),
                Role.USER,
                Provider.MANUAL
        );

        given(registerService.register("user@example.com", "secret123", "device-1", "Pixel"))
                .willReturn(result);

        mockMvc.perform(post("/public/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokens.accessToken").value("access-token"))
                .andExpect(jsonPath("$.user.id").value(userId.toString()))
                .andExpect(jsonPath("$.user.email").value("user@example.com"));

        verify(registerService).register("user@example.com", "secret123", "device-1", "Pixel");
    }

    @Test
    void publicAuthLogin_returnsAuthResponse() throws Exception {
        final DeviceInfo device = new DeviceInfo()
                .deviceId("device-2")
                .deviceName("MacBook")
                .platform(DeviceInfo.PlatformEnum.WEB);
        final LoginRequest request = new LoginRequest()
                .provider(com.skateboard.podcast.standardbe.api.model.Provider.MANUAL)
                .email("user@example.com")
                .password("secret123")
                .device(device);

        final UUID userId = UUID.randomUUID();
        final AuthResult result = new AuthResult(
                "access-token",
                900,
                "refresh-token",
                5184000,
                UserId.of(userId),
                Email.of("user@example.com"),
                Role.USER,
                Provider.MANUAL
        );

        given(loginService.login("user@example.com", "secret123", "device-2", "MacBook"))
                .willReturn(result);

        mockMvc.perform(post("/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokens.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user.id").value(userId.toString()));

        verify(loginService).login("user@example.com", "secret123", "device-2", "MacBook");
    }

    @Test
    void publicAuthRefresh_returnsAuthResponse() throws Exception {
        final RefreshRequest request = new RefreshRequest()
                .refreshToken("refresh-token")
                .deviceId("device-3");

        final UUID userId = UUID.randomUUID();
        final AuthResult result = new AuthResult(
                "new-access-token",
                900,
                "new-refresh-token",
                5184000,
                UserId.of(userId),
                Email.of("user@example.com"),
                Role.USER,
                Provider.MANUAL
        );

        given(refreshService.refresh("refresh-token", "device-3"))
                .willReturn(result);

        mockMvc.perform(post("/public/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokens.accessToken").value("new-access-token"));

        verify(refreshService).refresh("refresh-token", "device-3");
    }

    @Test
    void publicAuthLogout_returnsNoContent() throws Exception {
        final UUID userId = UUID.randomUUID();
        final CurrentUser principal = new CurrentUser(userId, "ADMIN", "admin@example.com");
        final var authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(post("/public/auth/logout"))
                .andExpect(status().isNoContent());

        verify(logoutService).logoutAllForUser(userId);
    }
}
