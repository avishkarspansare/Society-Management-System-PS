package com.societyledger.audit.service;

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
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditReportService {

    private final AuditReportRepository repository;
    private final AuditEventProducer eventProducer;

    @Value("${app.file-storage.base-path}")
    private String basePath;

    @Transactional
    public AuditReportResponse uploadAuditReport(Long societyId, String title, String description,
                                                  LocalDate periodFrom, LocalDate periodTo,
                                                  MultipartFile file, Long uploadedBy) {
        validateFile(file);

        String stored = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
        Path dir  = Paths.get(basePath, "audit", societyId.toString());
        Path dest = dir.resolve(stored);
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new SocietyLedgerException("Failed to store audit file: " + e.getMessage(),
                    "FILE_STORE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        AuditReport saved = repository.save(AuditReport.builder()
                .societyId(societyId).title(title).description(description)
                .periodFrom(periodFrom).periodTo(periodTo).uploadedBy(uploadedBy)
                .fileName(file.getOriginalFilename()).filePath(dest.toString())
                .fileSizeBytes(file.getSize()).mimeType(file.getContentType())
                .status(AuditReport.AuditStatus.UPLOADED)
                .build());

        eventProducer.publishAuditUploaded(societyId, saved.getId(), saved.getTitle());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditReportResponse> getReports(Long societyId, Pageable pageable) {
        return PageResponse.from(
                repository.findBySocietyIdOrderByCreatedAtDesc(societyId, pageable)
                          .map(this::mapToResponse));
    }

    @Transactional
    public AuditReportResponse publish(Long societyId, Long id) {
        AuditReport report = repository.findByIdAndSocietyId(id, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("AuditReport", id));
        report.setStatus(AuditReport.AuditStatus.PUBLISHED);
        return mapToResponse(repository.save(report));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new SocietyLedgerException("File is empty.", "EMPTY_FILE", HttpStatus.BAD_REQUEST);
        String ct = file.getContentType();
        if (ct == null || (!ct.equals("application/pdf") && !ct.startsWith("image/")))
            throw new SocietyLedgerException("Only PDF and image files are allowed.",
                    "INVALID_FILE_TYPE", HttpStatus.BAD_REQUEST);
    }

    private String sanitize(String name) {
        return name == null ? "audit_doc" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private AuditReportResponse mapToResponse(AuditReport r) {
        return AuditReportResponse.builder()
                .id(r.getId()).societyId(r.getSocietyId())
                .title(r.getTitle()).description(r.getDescription())
                .periodFrom(r.getPeriodFrom()).periodTo(r.getPeriodTo())
                .uploadedBy(r.getUploadedBy()).fileName(r.getFileName())
                .fileSizeBytes(r.getFileSizeBytes()).status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
