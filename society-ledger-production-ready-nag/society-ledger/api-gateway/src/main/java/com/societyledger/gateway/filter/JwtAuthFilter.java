package com.societyledger.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Gateway filter that validates JWT on all non-public routes.
 * Extracts claims and forwards them as headers to downstream services.
 *
 * Downstream services receive:
 *   X-User-Id:    (userId from JWT subject)
 *   X-Society-Id: (societyId from JWT claims)
 *   X-Flat-Id:    (flatId from JWT claims)
 *   X-User-Role:  (role from JWT claims)
 *   X-User-Email: (email from JWT claims)
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
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange, "Missing or invalid Authorization header.");
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Forward user context as request headers to downstream services
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id",    claims.getSubject())
                        .header("X-Society-Id", String.valueOf(claims.get("societyId")))
                        .header("X-Flat-Id",    String.valueOf(claims.get("flatId")))
                        .header("X-User-Role",  String.valueOf(claims.get("role")))
                        .header("X-User-Email", String.valueOf(claims.get("email")))
                        .build();

                log.debug("JWT validated for user {} society {}",
                        claims.getSubject(), claims.get("societyId"));

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (ExpiredJwtException e) {
                return unauthorized(exchange, "Token has expired. Please log in again.");
            } catch (JwtException e) {
                return unauthorized(exchange, "Invalid token. Please log in again.");
            }
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = "{\"success\":false,\"error\":\"" + message + "\",\"errorCode\":\"UNAUTHORIZED\"}";
        var buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public static class Config {
        // No config needed for now — extendable for role-based filtering per route
    }
}
