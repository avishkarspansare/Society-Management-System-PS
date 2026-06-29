package com.societyledger.audit.service;

import com.societyledger.audit.dto.response.AuditReportResponse;
import com.societyledger.audit.entity.AuditReport;
import com.societyledger.audit.repository.AuditReportRepository;
import com.societyledger.common.exception.SocietyLedgerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditReportService Unit Tests")
class AuditReportServiceTest {

    @Mock AuditReportRepository auditReportRepository;
    @InjectMocks AuditReportService auditReportService;

    private AuditReport sampleReport;

    @BeforeEach
    void setUp() {
        sampleReport = AuditReport.builder()
                .id(1L)
                .societyId(10L)
                .auditYear(2024)
                .auditorName("CA Rajan Mehta")
                .auditorFirm("Mehta & Associates")
                .complianceStatus(AuditReport.ComplianceStatus.COMPLIANT)
                .remarks("All accounts verified and found in order.")
                .reportFileName("audit-2024.pdf")
                .reportFilePath("/app/data/audit-reports/10/audit-2024.pdf")
                .uploadedBy(1L)
                .build();
    }

    @Test
    @DisplayName("getReports returns paged list for valid society")
    void getReports_returnsPaged() {
        var page = new PageImpl<>(List.of(sampleReport), PageRequest.of(0, 10), 1);
        when(auditReportRepository.findBySocietyIdOrderByAuditYearDesc(eq(10L), any()))
                .thenReturn(page);

        var result = auditReportService.getReports(10L, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAuditorName()).isEqualTo("CA Rajan Mehta");
    }

    @Test
    @DisplayName("getReportById throws NOT_FOUND for unknown id")
    void getReportById_throwsNotFound() {
        when(auditReportRepository.findByIdAndSocietyId(eq(99L), eq(10L)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditReportService.getReportById(10L, 99L))
                .isInstanceOf(SocietyLedgerException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("getReportById returns correct report for valid id")
    void getReportById_returnsReport() {
        when(auditReportRepository.findByIdAndSocietyId(eq(1L), eq(10L)))
                .thenReturn(Optional.of(sampleReport));

        var result = auditReportService.getReportById(10L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getAuditYear()).isEqualTo(2024);
        assertThat(result.getComplianceStatus()).isEqualTo("COMPLIANT");
    }
}
