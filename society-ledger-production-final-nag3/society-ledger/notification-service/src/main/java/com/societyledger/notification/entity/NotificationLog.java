package com.societyledger.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "notification_log",
    indexes = {
        @Index(name = "idx_notif_society", columnList = "society_id"),
        @Index(name = "idx_notif_flat",    columnList = "flat_id"),
        @Index(name = "idx_notif_event",   columnList = "event_type")
    }
)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "flat_id")
    private Long flatId;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(length = 30)
    @Builder.Default
    private String channel = "EMAIL";

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.SENT;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_at")
    @Builder.Default
    private Instant sentAt = Instant.now();

    public enum Status { SENT, FAILED, SKIPPED }
}
