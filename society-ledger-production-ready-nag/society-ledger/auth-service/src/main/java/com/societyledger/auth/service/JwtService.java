package com.societyledger.auth.service;

import com.societyledger.auth.entity.UserAccount;
import com.societyledger.common.security.JwtClaims;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiry-ms:900000}") // 15 minutes
    private long accessTokenExpiryMs;

    @Value("${app.jwt.refresh-token-expiry-ms:604800000}") // 7 days
    private long refreshTokenExpiryMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserAccount user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("societyId", user.getSocietyId());
        claims.put("flatId", user.getFlatId());
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        claims.put("type", "ACCESS");

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(user.getId()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiryMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshTokenValue() {
        return java.util.UUID.randomUUID().toString();
    }

    public long getRefreshTokenExpiryMs() {
        return refreshTokenExpiryMs;
    }

    public long getAccessTokenExpiryMs() {
        return accessTokenExpiryMs;
    }

    public JwtClaims extractClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return JwtClaims.builder()
                .userId(Long.parseLong(claims.getSubject()))
                .societyId(claims.get("societyId", Long.class))
                .flatId(claims.get("flatId", Long.class))
                .role(claims.get("role", String.class))
                .email(claims.get("email", String.class))
                .build();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
