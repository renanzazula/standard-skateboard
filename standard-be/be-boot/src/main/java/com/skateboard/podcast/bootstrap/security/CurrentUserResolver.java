package com.skateboard.podcast.bootstrap.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CurrentUserResolver {

    public Optional<CurrentUser> currentUser() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof final CurrentUser cu) {
            return Optional.of(cu);
        }
        return Optional.empty();
    }

    public CurrentUser requireUser() {
        return currentUser().orElseThrow(() -> new IllegalStateException("No authenticated user"));
    }

    public UUID requireUserId() {
        return requireUser().userId();
    }
}
