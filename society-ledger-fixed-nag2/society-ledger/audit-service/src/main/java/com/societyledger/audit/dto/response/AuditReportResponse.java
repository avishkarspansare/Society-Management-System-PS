package com.societyledger.audit.dto.response;

import lombok.*;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditReportResponse {
    private Long id;
    private Long societyId;
    private Integer auditYear;
    private String auditorName;
    private String auditorFirm;
    private String complianceStatus;
    private String remarks;
    private String issuesFound;
    private String reportFileName;
    private boolean hasReport;
    private Long uploadedBy;
    private Instant uploadedAt;
}
