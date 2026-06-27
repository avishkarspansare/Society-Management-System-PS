package com.societyledger.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Spring Cloud Gateway filter — validates JWT and injects user context headers.
 * Downstream services trust these headers; they do NOT re-validate the JWT.
 */
@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange, "Missing or malformed Authorization header");
            }
            String token = authHeader.substring(7);
            try {
                Claims claims = parseToken(token);
                ServerWebExchange mutated = exchange.mutate()
                        .request(r -> r.headers(h -> {
                            h.set("X-User-Id",    getString(claims, "userId"));
                            h.set("X-Society-Id", getString(claims, "societyId"));
                            h.set("X-Flat-Id",    getString(claims, "flatId"));
                            h.set("X-User-Role",  claims.get("role", String.class));
                            h.set("X-User-Email", claims.getSubject());
                        }))
                        .build();
                return chain.filter(mutated);
            } catch (Exception e) {
                log.warn("JWT validation failed: {}", e.getMessage());
                return unauthorized(exchange, "Invalid or expired token");
            }
        };
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }

    private String getString(Claims claims, String key) {
        Object val = claims.get(key);
        return val != null ? val.toString() : "";
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
        var body = exchange.getResponse().bufferFactory()
                .wrap(("{"success":false,"message":"" + reason + "","errorCode":"UNAUTHORIZED"}").getBytes());
        return exchange.getResponse().writeWith(Mono.just(body));
    }

    public static class Config {}
}
