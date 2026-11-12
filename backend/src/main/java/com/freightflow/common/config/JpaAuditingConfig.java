package com.freightflow.common.config;

import com.freightflow.common.security.AuthenticatedUser;
import com.freightflow.common.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Provides the current authenticated user id as the JPA auditor for @CreatedBy fields.
 */
@Configuration
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> SecurityUtils.currentUser().map(AuthenticatedUser::email);
    }
}
