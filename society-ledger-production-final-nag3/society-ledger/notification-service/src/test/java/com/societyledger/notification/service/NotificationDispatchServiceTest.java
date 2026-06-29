package com.societyledger.notification.service;

import com.societyledger.notification.repository.NotificationLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationDispatchService Unit Tests")
class NotificationDispatchServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock NotificationLogRepository logRepository;
    @InjectMocks NotificationDispatchService dispatchService;

    @Test
    @DisplayName("handleReceiptGenerated logs notification (mail disabled in test)")
    void handleReceiptGenerated_logsNotification() {
        Map<String, Object> event = Map.of(
            "societyId", 10,
            "flatId", 5,
            "receiptNumber", "RCP-2024-001",
            "residentName", "Jane Doe",
            "recipientEmail", "jane@test.com",
            "amount", "5000.00",
            "paymentMonth", 3,
            "paymentYear", 2024
        );

        // Should not throw even when mail is not configured in test
        // logRepository.save() should be called to record the notification
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        dispatchService.handleReceiptGenerated(event);

        verify(logRepository, atLeastOnce()).save(any());
    }
}
