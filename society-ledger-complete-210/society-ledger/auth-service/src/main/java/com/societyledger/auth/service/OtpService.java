package com.societyledger.auth.service;

import com.societyledger.auth.entity.OtpToken;
import com.societyledger.auth.entity.UserAccount;
import com.societyledger.auth.repository.OtpTokenRepository;
import com.societyledger.common.exception.SocietyLedgerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String OTP_TYPE_FORGOT_PASSWORD = "FORGOT_PASSWORD";
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_OTP_REQUESTS_PER_HOUR = 3;
    private final SecureRandom secureRandom = new SecureRandom();

    private final OtpTokenRepository otpTokenRepository;

    @Transactional
    public String generateAndSaveOtp(UserAccount user) {
        // Rate limiting: max 3 requests per hour
        long recentRequests = otpTokenRepository.countByUserAndOtpTypeAndCreatedAtAfter(
                user, OTP_TYPE_FORGOT_PASSWORD,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );
        if (recentRequests >= MAX_OTP_REQUESTS_PER_HOUR) {
            throw new SocietyLedgerException(
                    "Too many OTP requests. Please try again after an hour.",
                    "OTP_RATE_LIMIT",
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }

        // Invalidate existing OTPs
        otpTokenRepository.invalidateAllForUser(user, OTP_TYPE_FORGOT_PASSWORD);

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", secureRandom.nextInt(1_000_000));

        OtpToken otpToken = OtpToken.builder()
                .user(user)
                .otpCode(otpCode)
                .otpType(OTP_TYPE_FORGOT_PASSWORD)
                .expiresAt(Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES))
                .build();

        otpTokenRepository.save(otpToken);
        log.info("OTP generated for user: {}", user.getEmail());
        return otpCode;
    }

    @Transactional
    public void verifyOtp(UserAccount user, String otpCode) {
        OtpToken otpToken = otpTokenRepository
                .findTopByUserAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(user, OTP_TYPE_FORGOT_PASSWORD)
                .orElseThrow(() -> new SocietyLedgerException(
                        "No active OTP found. Please request a new one.",
                        "OTP_NOT_FOUND",
                        HttpStatus.BAD_REQUEST
                ));

        if (otpToken.isExpired()) {
            throw new SocietyLedgerException(
                    "OTP has expired. Please request a new one.",
                    "OTP_EXPIRED",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (!otpToken.getOtpCode().equals(otpCode)) {
            throw new SocietyLedgerException(
                    "Invalid OTP. Please check and try again.",
                    "OTP_INVALID",
                    HttpStatus.BAD_REQUEST
            );
        }

        otpToken.setIsUsed(true);
        otpTokenRepository.save(otpToken);
        log.info("OTP verified successfully for user: {}", user.getEmail());
    }

    @Scheduled(cron = "0 0 * * * *") // every hour
    @Transactional
    public void cleanupExpiredOtps() {
        otpTokenRepository.deleteExpiredTokens(Instant.now());
        log.debug("Expired OTP tokens cleaned up");
    }
}
