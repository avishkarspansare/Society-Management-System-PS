package com.societyledger.audit.dto.request;

import com.societyledger.audit.entity.AuditReport;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadAuditReportRequest {
    @NotNull(message = "Audit year is required")
    private Integer auditYear;
    @NotBlank(message = "Auditor name is required")
    private String auditorName;
    private String auditorFirm;
    @NotNull(message = "Compliance status is required")
    private AuditReport.ComplianceStatus complianceStatus;
    private String remarks;
    private String issuesFound;
}
