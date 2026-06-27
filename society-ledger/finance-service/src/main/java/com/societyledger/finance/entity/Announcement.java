package com.societyledger.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "announcements",
    indexes = @Index(name = "idx_announcements_society", columnList = "society_id"))
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Announcement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "society_id", nullable = false) private Long societyId;
    @Column(name = "title", nullable = false, length = 255) private String title;
    @Column(name = "content", columnDefinition = "TEXT", nullable = false) private String content;
    @Column(name = "created_by") private Long createdBy;
    @Column(name = "is_pinned") @Builder.Default private Boolean isPinned = false;

    @CreatedDate @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
