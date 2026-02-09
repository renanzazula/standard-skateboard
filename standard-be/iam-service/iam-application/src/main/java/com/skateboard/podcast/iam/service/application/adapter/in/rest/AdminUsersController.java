package com.skateboard.podcast.iam.service.application.adapter.in.rest;

import com.skateboard.podcast.domain.security.CurrentUser;
import com.skateboard.podcast.iam.service.application.port.in.AdminUsersUseCase;
import com.skateboard.podcast.standardbe.api.AdminUsersApi;
import com.skateboard.podcast.standardbe.api.model.AdminUser;
import com.skateboard.podcast.standardbe.api.model.AdminUserStatusUpdateRequest;
import com.skateboard.podcast.standardbe.api.model.AdminUserUpdateRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class AdminUsersController implements AdminUsersApi {

    private final AdminUsersUseCase adminUsersUseCase;
    private final AdminUsersApiMapper mapper;

    public AdminUsersController(
            final AdminUsersUseCase adminUsersUseCase,
            final AdminUsersApiMapper mapper
    ) {
        this.adminUsersUseCase = adminUsersUseCase;
        this.mapper = mapper;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    @GetMapping(value = "/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AdminUser>> adminUserList() {
        final List<AdminUser> users = adminUsersUseCase.list().stream()
                .map(mapper::toApi)
                .toList();
        return ResponseEntity.ok(users);
    }

    @Override
    @PutMapping(
            value = "/admin/users/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AdminUser> adminUserUpdate(final UUID id, final AdminUserUpdateRequest adminUserUpdateRequest) {
        final var updated = adminUsersUseCase.update(id, mapper.toCommand(adminUserUpdateRequest), requireUserId());
        return ResponseEntity.ok(mapper.toApi(updated));
    }

    @Override
    @PatchMapping(
            value = "/admin/users/{id}/status",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AdminUser> adminUserStatusUpdate(
            final UUID id,
            final AdminUserStatusUpdateRequest adminUserStatusUpdateRequest
    ) {
        final var updated = adminUsersUseCase.updateStatus(
                id,
                mapper.toStatus(adminUserStatusUpdateRequest),
                requireUserId()
        );
        return ResponseEntity.ok(mapper.toApi(updated));
    }

    @Override
    @DeleteMapping(value = "/admin/users/{id}")
    public ResponseEntity<Void> adminUserDelete(final UUID id) {
        adminUsersUseCase.delete(id, requireUserId());
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
