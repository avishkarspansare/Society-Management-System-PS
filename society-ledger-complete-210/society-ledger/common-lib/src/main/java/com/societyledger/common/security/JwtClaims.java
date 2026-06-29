package com.societyledger.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parsed JWT claims passed as Spring Security principal in downstream services.
 * Populated by the gateway-injected X-User-* headers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {

    private Long userId;
    private Long societyId;
    private Long flatId;
    private String role;
    private String email;

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isResident() {
        return "RESIDENT".equalsIgnoreCase(role);
    }
}
