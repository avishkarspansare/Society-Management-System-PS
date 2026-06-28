package com.societyledger.finance.security;

import com.societyledger.common.security.JwtClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId    = request.getHeader("X-User-Id");
        String societyId = request.getHeader("X-Society-Id");
        String flatId    = request.getHeader("X-Flat-Id");
        String role      = request.getHeader("X-User-Role");
        String email     = request.getHeader("X-User-Email");

        if (userId != null && role != null) {
            JwtClaims claims = JwtClaims.builder()
                    .userId(parseLong(userId))
                    .societyId(parseLong(societyId))
                    .flatId(parseLong(flatId))
                    .role(role)
                    .email(email)
                    .build();
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(claims, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private Long parseLong(String val) {
        if (val == null || val.equals("null")) return null;
        try { return Long.parseLong(val); } catch (NumberFormatException e) { return null; }
    }
}
