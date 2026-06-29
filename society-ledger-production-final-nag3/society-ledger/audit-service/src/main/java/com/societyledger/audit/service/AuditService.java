package com.societyledger.audit.service;

import com.societyledger.audit.dto.request.UploadAuditReportRequest;
import com.societyledger.audit.dto.response.AuditReportResponse;
import com.societyledger.audit.entity.AuditReport;
import com.societyledger.audit.kafka.AuditEventProducer;
import com.societyledger.audit.repository.AuditReportRepository;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.exception.SocietyLedgerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditReportRepository auditReportRepository;
    private final AuditEventProducer eventProducer;

    @Value("${app.audit.storage-dir:/app/data/audit-reports}")
    private String storageDir;

    @Transactional
    public AuditReportResponse uploadReport(Long societyId, MultipartFile file,
                                             UploadAuditReportRequest request,
                                             Long uploadedBy) {
        if (file != null && !file.isEmpty()) {
            validateFile(file);
        }

        AuditReport report = AuditReport.builder()
                .societyId(societyId)
                .auditYear(request.getAuditYear())
                .auditorName(request.getAuditorName().trim())
                .auditorFirm(request.getAuditorFirm())
                .complianceStatus(request.getComplianceStatus())
                .remarks(request.getRemarks())
                .issuesFound(request.getIssuesFound())
                .uploadedBy(uploadedBy)
                .build();

        if (file != null && !file.isEmpty()) {
            String filePath = saveFile(societyId, file);
            report.setReportFilePath(filePath);
            report.setReportFileName(file.getOriginalFilename());
        }

        AuditReport saved = auditReportRepository.save(report);
        log.info("Audit report uploaded for society {} year {}", societyId, request.getAuditYear());

        // Publish Kafka event — notifies all residents
        eventProducer.publishAuditUploaded(saved);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditReportResponse> getReports(Long societyId, Pageable pageable) {
        return PageResponse.from(
                auditReportRepository.findBySocietyIdOrderByAuditYearDescUploadedAtDesc(
                        societyId, pageable).map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public AuditReportResponse getReportById(Long societyId, Long reportId) {
        return auditReportRepository.findByIdAndSocietyId(reportId, societyId)
                .map(this::mapToResponse)
                .orElseThrow(() -> SocietyLedgerException.notFound("AuditReport", reportId));
    }

    @Transactional(readOnly = true)
    public byte[] downloadReport(Long societyId, Long reportId) {
        AuditReport report = auditReportRepository.findByIdAndSocietyId(reportId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("AuditReport", reportId));

        if (report.getReportFilePath() == null) {
            throw SocietyLedgerException.badRequest("No file attached to this audit report.");
        }

        try {
            return Files.readAllBytes(Paths.get(report.getReportFilePath()));
        } catch (IOException e) {
            log.error("Failed to read audit report file {}: {}", report.getReportFilePath(), e.getMessage());
            throw new SocietyLedgerException("Audit report file could not be read.",
                    "FILE_READ_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > 25 * 1024 * 1024) {
            throw SocietyLedgerException.badRequest("Audit report file cannot exceed 25 MB.");
        }
        String ct = file.getContentType();
        if (ct == null || (!ct.equals("application/pdf") && !ct.startsWith("image/"))) {
            throw SocietyLedgerException.badRequest("Audit report must be a PDF or image file.");
        }
    }

    private String saveFile(Long societyId, MultipartFile file) {
        try {
            Path dir = Paths.get(storageDir, String.valueOf(societyId));
            Files.createDirectories(dir);
            String name = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path dest = dir.resolve(name);
            file.transferTo(dest.toFile());
            return dest.toString();
        } catch (IOException e) {
            throw new SocietyLedgerException("Failed to store audit file.",
                    "FILE_STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private AuditReportResponse mapToResponse(AuditReport r) {
        return AuditReportResponse.builder()
                .id(r.getId()).societyId(r.getSocietyId())
                .auditYear(r.getAuditYear()).auditorName(r.getAuditorName())
                .auditorFirm(r.getAuditorFirm())
                .complianceStatus(r.getComplianceStatus().name())
                .remarks(r.getRemarks()).issuesFound(r.getIssuesFound())
                .reportFileName(r.getReportFileName())
                .hasReport(r.getReportFilePath() != null)
                .uploadedBy(r.getUploadedBy()).uploadedAt(r.getUploadedAt())
                .build();
    }
}
