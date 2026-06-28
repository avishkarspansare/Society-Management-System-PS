package com.societyledger.auth.service;

import com.societyledger.auth.entity.OtpToken;
import com.societyledger.auth.entity.UserAccount;
import com.societyledger.auth.repository.OtpTokenRepository;
import com.societyledger.common.exception.SocietyLedgerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OtpService Tests")
class OtpServiceTest {

    @Mock OtpTokenRepository otpTokenRepository;
    @InjectMocks OtpService otpService;

    private UserAccount user;

    @BeforeEach
    void setUp() {
        user = UserAccount.builder()
                .id(1L).email("test@example.com")
                .societyId(1L).flatId(1L)
                .passwordHash("hash")
                .role(UserAccount.UserRole.RESIDENT)
                .isActive(true).build();
    }

    @Test
    @DisplayName("Generated OTP is exactly 6 digits")
    void testOtpIsSixDigits() {
        when(otpTokenRepository.countByUserAndOtpTypeAndCreatedAtAfter(any(), any(), any()))
                .thenReturn(0L);
        when(otpTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String otp = otpService.generateAndSaveOtp(user);

        assertThat(otp).matches("\\d{6}");
    }

    @Test
    @DisplayName("OTP is between 000000 and 999999")
    void testOtpIsInRange() {
        when(otpTokenRepository.countByUserAndOtpTypeAndCreatedAtAfter(any(), any(), any()))
                .thenReturn(0L);
        when(otpTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        for (int i = 0; i < 20; i++) {
            String otp = otpService.generateAndSaveOtp(user);
            int num = Integer.parseInt(otp);
            assertThat(num).isBetween(0, 999999);
        }
    }

    @Test
    @DisplayName("Rate limit throws after 3 OTP requests")
    void testRateLimitEnforced() {
        when(otpTokenRepository.countByUserAndOtpTypeAndCreatedAtAfter(any(), any(), any()))
                .thenReturn(3L);

        assertThatThrownBy(() -> otpService.generateAndSaveOtp(user))
                .isInstanceOf(SocietyLedgerException.class)
                .hasMessageContaining("Too many OTP requests");
    }

    @Test
    @DisplayName("Valid OTP verification succeeds")
    void testValidOtpVerificationSucceeds() {
        OtpToken token = OtpToken.builder()
                .user(user).otpCode("123456")
                .otpType("FORGOT_PASSWORD")
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .isUsed(false).build();

        when(otpTokenRepository.findTopByUserAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(
                any(), any())).thenReturn(Optional.of(token));
        when(otpTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatCode(() -> otpService.verifyOtp(user, "123456"))
                .doesNotThrowAnyException();
        assertThat(token.getIsUsed()).isTrue();
    }

    @Test
    @DisplayName("Wrong OTP throws SocietyLedgerException")
    void testWrongOtpThrows() {
        OtpToken token = OtpToken.builder()
                .user(user).otpCode("123456")
                .otpType("FORGOT_PASSWORD")
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .isUsed(false).build();

        when(otpTokenRepository.findTopByUserAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(
                any(), any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> otpService.verifyOtp(user, "999999"))
                .isInstanceOf(SocietyLedgerException.class)
                .hasMessageContaining("Invalid OTP");
    }

    @Test
    @DisplayName("Expired OTP throws SocietyLedgerException")
    void testExpiredOtpThrows() {
        OtpToken expiredToken = OtpToken.builder()
                .user(user).otpCode("123456")
                .otpType("FORGOT_PASSWORD")
                .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES)) // already expired
                .isUsed(false).build();

        when(otpTokenRepository.findTopByUserAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(
                any(), any())).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> otpService.verifyOtp(user, "123456"))
                .isInstanceOf(SocietyLedgerException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("No active OTP throws SocietyLedgerException")
    void testNoOtpFoundThrows() {
        when(otpTokenRepository.findTopByUserAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(
                any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> otpService.verifyOtp(user, "123456"))
                .isInstanceOf(SocietyLedgerException.class)
                .hasMessageContaining("No active OTP");
    }
}
