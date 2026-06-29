package com.societyledger.audit.dto.response;

import com.societyledger.audit.entity.AuditReport;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;

@Data @Builder
public class AuditReportResponse {
    Long id;
    Long societyId;
    String title;
    String description;
    LocalDate periodFrom;
    LocalDate periodTo;
    Long uploadedBy;
    String fileName;
    Long fileSizeBytes;
    AuditReport.AuditStatus status;
    Instant createdAt;
}
