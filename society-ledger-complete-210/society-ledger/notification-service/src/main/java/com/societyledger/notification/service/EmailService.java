package com.societyledger.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@societyledger.com}")
    private String fromEmail;

    /** Notify resident their maintenance payment was received. */
    @Async
    public void sendReceiptNotification(Long societyId, Long flatId, Long receiptId, BigDecimal amount) {
        log.info("[EMAIL] Maintenance receipt #{} for flat {} society {} amount {}",
                receiptId, flatId, societyId, amount);
        // TODO: Fetch resident email via SocietyServiceClient Feign call and send real email
        // Placeholder: just log. Uncomment and adapt when society-service Feign is wired.
        /*
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(residentEmail);
            msg.setSubject("Maintenance Receipt - Society Ledger");
            msg.setText("Your payment of ₹" + amount + " has been received. Receipt #" + receiptId);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send receipt email: {}", e.getMessage());
        }
        */
    }

    @Async
    public void sendQueryAnsweredNotification(Long societyId, Long queryId, Long userId) {
        log.info("[EMAIL] Query #{} answered in society {} for user {}", queryId, societyId, userId);
    }

    @Async
    public void sendAuditUploadedNotification(Long societyId, String title) {
        log.info("[EMAIL] Audit report '{}' uploaded in society {}", title, societyId);
    }

    @Async
    public void sendExpensePublishedNotification(Long societyId, Long expenseId, String vendor, BigDecimal amount) {
        log.info("[EMAIL] Expense #{} published in society {} vendor={} amount={}", expenseId, societyId, vendor, amount);
    }

    @Async
    public void sendOtp(String toEmail, String otp) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(toEmail);
            msg.setSubject("Society Ledger - Password Reset OTP");
            msg.setText("Your OTP for password reset is: " + otp +
                        "\n\nThis OTP is valid for 10 minutes.\n\n" +
                        "If you did not request this, please ignore this email.");
            mailSender.send(msg);
            log.info("OTP sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }
}
