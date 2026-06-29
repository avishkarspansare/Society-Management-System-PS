package com.societyledger.query.security;
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
@Slf4j @Component
public class GatewayAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String userId = req.getHeader("X-User-Id");
        String role   = req.getHeader("X-User-Role");
        if (userId != null && role != null) {
            JwtClaims claims = JwtClaims.builder()
                .userId(parseLong(userId)).societyId(parseLong(req.getHeader("X-Society-Id")))
                .flatId(parseLong(req.getHeader("X-Flat-Id"))).role(role)
                .email(req.getHeader("X-User-Email")).build();
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(claims, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
        }
        chain.doFilter(req, res);
    }
    private Long parseLong(String v) { if (v==null||v.equals("null")) return null; try { return Long.parseLong(v); } catch (NumberFormatException e) { return null; } }
}
