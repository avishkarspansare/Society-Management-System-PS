package com.societyledger.auth.service;

import com.societyledger.auth.dto.request.*;
import com.societyledger.auth.dto.response.LoginResponse;
import com.societyledger.auth.dto.response.UserProfileResponse;
import com.societyledger.auth.entity.RefreshToken;
import com.societyledger.auth.entity.UserAccount;
import com.societyledger.auth.feign.SocietyServiceClient;
import com.societyledger.auth.repository.RefreshTokenRepository;
import com.societyledger.auth.repository.UserAccountRepository;
import com.societyledger.common.exception.SocietyLedgerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final MailService mailService;
    private final SocietyServiceClient societyServiceClient;

    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        // Validate society and flat exist
        societyServiceClient.validateFlat(request.getSocietyId(), request.getFlatId());

        // One account per flat
        if (userAccountRepository.existsByFlatIdAndSocietyId(request.getFlatId(), request.getSocietyId())) {
            throw new SocietyLedgerException(
                    "An account already exists for this flat.",
                    "FLAT_ALREADY_REGISTERED",
                    HttpStatus.CONFLICT
            );
        }

        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new SocietyLedgerException(
                    "Email is already in use.",
                    "EMAIL_ALREADY_EXISTS",
                    HttpStatus.CONFLICT
            );
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw SocietyLedgerException.badRequest("Passwords do not match.");
        }

        UserAccount user = UserAccount.builder()
                .societyId(request.getSocietyId())
                .flatId(request.getFlatId())
                .email(request.getEmail().toLowerCase().strip())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserAccount.UserRole.RESIDENT)
                .build();

        userAccountRepository.save(user);
        log.info("New user registered: {} for flat {}", user.getEmail(), user.getFlatId());

        return mapToProfile(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByEmail(request.getEmail().toLowerCase().strip())
                .orElseThrow(() -> new SocietyLedgerException(
                        "Invalid email or password.",
                        "INVALID_CREDENTIALS",
                        HttpStatus.UNAUTHORIZED
                ));

        if (!user.getIsActive()) {
            throw new SocietyLedgerException(
                    "Account is deactivated. Please contact your society admin.",
                    "ACCOUNT_INACTIVE",
                    HttpStatus.FORBIDDEN
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new SocietyLedgerException(
                    "Invalid email or password.",
                    "INVALID_CREDENTIALS",
                    HttpStatus.UNAUTHORIZED
            );
        }

        // Revoke all previous refresh tokens
        refreshTokenRepository.revokeAllForUser(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshTokenValue();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpiryMs()))
                .build();
        refreshTokenRepository.save(refreshToken);

        log.info("User logged in: {}", user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .expiresIn(jwtService.getAccessTokenExpiryMs() / 1000)
                .role(user.getRole().name())
                .societyId(user.getSocietyId())
                .flatId(user.getFlatId())
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new SocietyLedgerException(
                        "Invalid or expired refresh token.",
                        "INVALID_REFRESH_TOKEN",
                        HttpStatus.UNAUTHORIZED
                ));

        if (storedToken.getRevoked() || storedToken.isExpired()) {
            throw new SocietyLedgerException(
                    "Refresh token is expired or revoked.",
                    "REFRESH_TOKEN_EXPIRED",
                    HttpStatus.UNAUTHORIZED
            );
        }

        UserAccount user = storedToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshTokenValue = jwtService.generateRefreshTokenValue();

        // Rotate refresh token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newRefreshTokenValue)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpiryMs()))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .expiresIn(jwtService.getAccessTokenExpiryMs() / 1000)
                .role(user.getRole().name())
                .societyId(user.getSocietyId())
                .flatId(user.getFlatId())
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void initiateForgotPassword(ForgotPasswordRequest request) {
        UserAccount user = userAccountRepository.findByEmail(request.getEmail().toLowerCase().strip())
                .orElseThrow(() -> new SocietyLedgerException(
                        "No account found with this email.",
                        "EMAIL_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        String otp = otpService.generateAndSaveOtp(user);
        mailService.sendOtpEmail(user.getEmail(), otp);
        log.info("Forgot password OTP sent to: {}", user.getEmail());
    }

    @Transactional
    public void verifyOtp(OtpVerifyRequest request) {
        UserAccount user = userAccountRepository.findByEmail(request.getEmail().toLowerCase().strip())
                .orElseThrow(() -> SocietyLedgerException.notFound("User", -1L));
        otpService.verifyOtp(user, request.getOtpCode());
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw SocietyLedgerException.badRequest("Passwords do not match.");
        }

        UserAccount user = userAccountRepository.findByEmail(request.getEmail().toLowerCase().strip())
                .orElseThrow(() -> SocietyLedgerException.notFound("User", -1L));

        // OTP must be verified before reaching here; verify again for security
        otpService.verifyOtp(user, request.getOtpCode());

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userAccountRepository.save(user);

        // Revoke all refresh tokens after password change
        refreshTokenRepository.revokeAllForUser(user);
        log.info("Password reset successful for: {}", user.getEmail());
    }

    public UserProfileResponse getProfile(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> SocietyLedgerException.notFound("User", userId));
        return mapToProfile(user);
    }

    private UserProfileResponse mapToProfile(UserAccount user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .societyId(user.getSocietyId())
                .flatId(user.getFlatId())
                .isActive(user.getIsActive())
                .build();
    }
}
