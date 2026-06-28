package com.societyledger.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Society Ledger - Password Reset OTP");
            message.setText(buildOtpEmailBody(otp));
            mailSender.send(message);
            log.info("OTP email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildOtpEmailBody(String otp) {
        return """
                Hello,
                
                Your password reset OTP for Society Ledger is:
                
                %s
                
                This OTP is valid for 10 minutes. Do not share it with anyone.
                
                If you did not request this, please ignore this email.
                
                Regards,
                Society Ledger Team
                """.formatted(otp);
    }
}
