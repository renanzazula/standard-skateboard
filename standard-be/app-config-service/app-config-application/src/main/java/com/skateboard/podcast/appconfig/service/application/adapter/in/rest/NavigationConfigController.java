package com.skateboard.podcast.appconfig.service.application.adapter.in.rest;

import com.skateboard.podcast.appconfig.service.application.port.in.NavigationConfigUseCase;
import com.skateboard.podcast.domain.security.CurrentUser;
import com.skateboard.podcast.standardbe.api.AdminNavigationConfigApi;
import com.skateboard.podcast.standardbe.api.PublicNavigationConfigApi;
import com.skateboard.podcast.standardbe.api.model.NavigationConfig;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@RestController
public class NavigationConfigController implements PublicNavigationConfigApi, AdminNavigationConfigApi {

    private final NavigationConfigUseCase navigationConfigUseCase;
    private final NavigationConfigApiMapper mapper;

    public NavigationConfigController(
            final NavigationConfigUseCase navigationConfigUseCase,
            final NavigationConfigApiMapper mapper
    ) {
        this.navigationConfigUseCase = navigationConfigUseCase;
        this.mapper = mapper;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    @GetMapping(
            value = "/public/navigation-config",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<NavigationConfig> publicNavigationConfigGet() {
        final CurrentUser currentUser = currentUser();
        if (currentUser == null) {
            return ResponseEntity.ok(new NavigationConfig().tabs(List.of()));
        }
        return ResponseEntity.ok(mapper.toApi(navigationConfigUseCase.get()));
    }

    @Override
    @PutMapping(
            value = "/admin/navigation-config",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<NavigationConfig> adminNavigationConfigUpdate(final NavigationConfig navigationConfig) {
        final var updated = navigationConfigUseCase.update(mapper.toView(navigationConfig));
        return ResponseEntity.ok(mapper.toApi(updated));
    }

    private static CurrentUser currentUser() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (auth.getPrincipal() instanceof final CurrentUser currentUser) {
            return currentUser;
        }
        return null;
    }
}
