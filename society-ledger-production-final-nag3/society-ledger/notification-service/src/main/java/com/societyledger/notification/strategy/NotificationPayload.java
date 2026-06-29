package com.societyledger.notification.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {
    private String recipientEmail;
    private String recipientPhone;
    private String subject;
    private String body;
    private String eventType;
    private Long societyId;
    private Long flatId;
}
