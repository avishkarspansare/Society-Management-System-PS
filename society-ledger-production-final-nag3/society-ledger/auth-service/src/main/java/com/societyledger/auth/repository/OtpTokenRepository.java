package com.societyledger.auth.repository;

import com.societyledger.auth.entity.OtpToken;
import com.societyledger.auth.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByUserAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(
            UserAccount user, String otpType);

    @Modifying
    @Query("UPDATE OtpToken o SET o.isUsed = true WHERE o.user = :user AND o.otpType = :otpType")
    void invalidateAllForUser(UserAccount user, String otpType);

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :cutoff")
    void deleteExpiredTokens(Instant cutoff);

    long countByUserAndOtpTypeAndCreatedAtAfter(UserAccount user, String otpType, Instant after);
}
