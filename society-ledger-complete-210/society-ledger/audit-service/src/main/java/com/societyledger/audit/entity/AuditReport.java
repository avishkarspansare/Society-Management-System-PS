package com.societyledger.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "audit_reports",
    indexes = {
        @Index(name = "idx_audit_society", columnList = "society_id"),
        @Index(name = "idx_audit_period", columnList = "period_from,period_to")
    })
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuditReport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false) private Long societyId;
    @Column(name = "title", nullable = false, length = 255) private String title;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "period_from") private LocalDate periodFrom;
    @Column(name = "period_to") private LocalDate periodTo;
    @Column(name = "uploaded_by") private Long uploadedBy;
    @Column(name = "file_name", length = 255) private String fileName;
    @Column(name = "file_path", length = 512) private String filePath;
    @Column(name = "file_size_bytes") private Long fileSizeBytes;
    @Column(name = "mime_type", length = 100) private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    @Builder.Default
    private AuditStatus status = AuditStatus.UPLOADED;

    @CreatedDate @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum AuditStatus { UPLOADED, REVIEWED, PUBLISHED, ARCHIVED }
}
