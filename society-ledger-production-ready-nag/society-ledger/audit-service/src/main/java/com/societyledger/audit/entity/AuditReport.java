package com.societyledger.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "audit_reports",
    indexes = {
        @Index(name = "idx_audit_society", columnList = "society_id"),
        @Index(name = "idx_audit_year",    columnList = "society_id, audit_year")
    }
)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuditReport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "audit_year", nullable = false)
    private Integer auditYear;

    @Column(name = "auditor_name", nullable = false, length = 255)
    private String auditorName;

    @Column(name = "auditor_firm", length = 255)
    private String auditorFirm;

    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_status", nullable = false, length = 20)
    private ComplianceStatus complianceStatus;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "issues_found", columnDefinition = "TEXT")
    private String issuesFound;

    @Column(name = "report_file_path", length = 500)
    private String reportFilePath;

    @Column(name = "report_file_name", length = 255)
    private String reportFileName;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "uploaded_at") @Builder.Default
    private Instant uploadedAt = Instant.now();

    public enum ComplianceStatus {
        COMPLIANT, NON_COMPLIANT, PENDING
    }
}
