package com.societyledger.receipt.config;

import com.societyledger.common.security.JwtClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Downstream service security config.
 * JWT validation is done at the API Gateway.
 * Services trust the X-User-* headers injected by the gateway.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(gatewayHeaderFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public OncePerRequestFilter gatewayHeaderFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {

                String userId    = request.getHeader("X-User-Id");
                String societyId = request.getHeader("X-Society-Id");
                String flatId    = request.getHeader("X-Flat-Id");
                String role      = request.getHeader("X-User-Role");
                String email     = request.getHeader("X-User-Email");

                if (userId != null && role != null) {
                    JwtClaims claims = JwtClaims.builder()
                            .userId(Long.parseLong(userId))
                            .societyId(societyId != null ? Long.parseLong(societyId) : null)
                            .flatId(flatId != null ? Long.parseLong(flatId) : null)
                            .role(role)
                            .email(email)
                            .build();

                    var auth = new UsernamePasswordAuthenticationToken(
                            claims, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                filterChain.doFilter(request, response);
            }
        };
    }
}
