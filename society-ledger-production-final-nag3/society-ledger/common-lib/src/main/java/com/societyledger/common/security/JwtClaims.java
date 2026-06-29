package com.societyledger.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
