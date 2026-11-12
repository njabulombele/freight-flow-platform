package com.freightflow.common.security;

import java.util.UUID;

/**
 * Lightweight principal attached to the security context from the JWT claims.
 */
public record AuthenticatedUser(
        UUID userId,
        String email,
        String tenantId,
        String role
) {
    public String authority() {
        return "ROLE_" + role;
    }
}
