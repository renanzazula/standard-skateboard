package com.skateboard.podcast.domain.security;

import java.util.UUID;

public record CurrentUser(UUID userId, String role, String email) {
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
