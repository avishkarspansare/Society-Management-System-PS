package com.societyledger.auth.service;

import com.societyledger.auth.entity.UserAccount;
import com.societyledger.common.security.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "society-ledger-super-secret-key-must-be-at-least-256-bits-long-for-hs256";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiryMs", 900_000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiryMs", 604_800_000L);
    }

    private UserAccount buildUser() {
        return UserAccount.builder()
                .id(42L)
                .societyId(1L)
                .flatId(10L)
                .email("b403@example.com")
                .passwordHash("hashed")
                .role(UserAccount.UserRole.RESIDENT)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Generated access token is not null or blank")
    void testTokenIsGenerated() {
        String token = jwtService.generateAccessToken(buildUser());
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Generated token is valid immediately after creation")
    void testTokenIsValid() {
        String token = jwtService.generateAccessToken(buildUser());
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("Extracted claims match the user passed in")
    void testClaimsExtraction() {
        UserAccount user = buildUser();
        String token = jwtService.generateAccessToken(user);
        JwtClaims claims = jwtService.extractClaims(token);

        assertThat(claims.getUserId()).isEqualTo(42L);
        assertThat(claims.getSocietyId()).isEqualTo(1L);
        assertThat(claims.getFlatId()).isEqualTo(10L);
        assertThat(claims.getRole()).isEqualTo("RESIDENT");
        assertThat(claims.getEmail()).isEqualTo("b403@example.com");
    }

    @Test
    @DisplayName("Admin role is preserved in claims")
    void testAdminRoleClaim() {
        UserAccount admin = UserAccount.builder()
                .id(1L).societyId(1L).flatId(1L)
                .email("admin@example.com").passwordHash("hash")
                .role(UserAccount.UserRole.ADMIN).isActive(true).build();

        String token = jwtService.generateAccessToken(admin);
        JwtClaims claims = jwtService.extractClaims(token);

        assertThat(claims.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Tampered token is not valid")
    void testTamperedTokenRejected() {
        String token = jwtService.generateAccessToken(buildUser());
        String tampered = token + "tampered";
        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("Random string is not a valid token")
    void testRandomStringRejected() {
        assertThat(jwtService.isTokenValid("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("Null or empty token is not valid")
    void testNullOrEmptyRejected() {
        assertThat(jwtService.isTokenValid(null)).isFalse();
        assertThat(jwtService.isTokenValid("")).isFalse();
        assertThat(jwtService.isTokenValid("  ")).isFalse();
    }

    @Test
    @DisplayName("Refresh token value is a non-null UUID string")
    void testRefreshTokenGenerated() {
        String refreshToken = jwtService.generateRefreshTokenValue();
        assertThat(refreshToken).isNotNull().isNotBlank();
        assertThat(refreshToken).matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("Multiple calls produce different refresh token values")
    void testRefreshTokensAreUnique() {
        String rt1 = jwtService.generateRefreshTokenValue();
        String rt2 = jwtService.generateRefreshTokenValue();
        assertThat(rt1).isNotEqualTo(rt2);
    }
}
