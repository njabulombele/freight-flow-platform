package com.freightflow.common.security;

import com.freightflow.common.tenant.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Helpers for reading the authenticated principal from the security context.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<AuthenticatedUser> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public static String currentTenantId() {
        AuthenticatedUser user = currentUser().orElse(null);
        if (user != null) {
            return user.tenantId();
        }
        return TenantContext.getTenantId();
    }
}
