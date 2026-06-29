package com.societyledger.receipt.config;

import com.societyledger.common.security.JwtClaims;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class JpaAuditingConfig {

    /**
     * Supplies the current user ID for JPA audit fields (createdBy, updatedBy).
     * Returns empty when no authentication is present (e.g., Flyway migration, scheduled jobs).
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof JwtClaims claims)) {
                return Optional.empty();
            }
            return Optional.ofNullable(claims.getUserId());
        };
    }
}
