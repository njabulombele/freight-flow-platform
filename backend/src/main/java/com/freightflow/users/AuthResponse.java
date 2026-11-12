package com.freightflow.users;

import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String email,
        String tenantId,
        String role,
        String firstName,
        String lastName
) {
}
