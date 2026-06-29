package com.societyledger.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "announcements",
    indexes = @Index(name = "idx_ann_society", columnList = "society_id, is_active")
)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Announcement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "society_id", nullable = false)
    private Long societyId;
    @Column(nullable = false, length = 255)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;
    @Column(length = 50)
    private String category;
    @Column(name = "is_active") @Builder.Default
    private Boolean isActive = true;
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    @Column(name = "created_at") @Builder.Default
    private Instant createdAt = Instant.now();
    @Column(name = "expires_at")
    private Instant expiresAt;
}
