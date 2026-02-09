package com.skateboard.podcast.iam.service.application.adapter.in.rest;

import com.skateboard.podcast.domain.security.CurrentUser;
import com.skateboard.podcast.iam.service.application.port.in.UserProfileUseCase;
import com.skateboard.podcast.standardbe.api.UserProfileApi;
import com.skateboard.podcast.standardbe.api.model.UserProfile;
import com.skateboard.podcast.standardbe.api.model.UserProfileUpdateRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;
import java.util.UUID;

@RestController
public class UserProfileController implements UserProfileApi {

    private final UserProfileUseCase userProfileUseCase;
    private final UserProfileApiMapper mapper;

    public UserProfileController(
            final UserProfileUseCase userProfileUseCase,
            final UserProfileApiMapper mapper
    ) {
        this.userProfileUseCase = userProfileUseCase;
        this.mapper = mapper;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    @GetMapping(value = "/users/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserProfile> userProfileGet() {
        final var profile = userProfileUseCase.get(requireUserId());
        return ResponseEntity.ok(mapper.toApi(profile));
    }

    @Override
    @PutMapping(
            value = "/users/me",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserProfile> userProfileUpdate(final UserProfileUpdateRequest userProfileUpdateRequest) {
        final var updated = userProfileUseCase.update(
                requireUserId(),
                mapper.toCommand(userProfileUpdateRequest)
        );
        return ResponseEntity.ok(mapper.toApi(updated));
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
