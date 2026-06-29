package com.societyledger.receipt.service;

import com.societyledger.receipt.entity.Receipt;
import com.societyledger.receipt.feign.SocietyServiceClient;
import com.societyledger.receipt.feign.StatementServiceClient;
import com.societyledger.receipt.repository.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReceiptService Unit Tests")
class ReceiptServiceTest {

    @Mock  ReceiptRepository receiptRepository;
    @Mock  SocietyServiceClient societyClient;
    @Mock  StatementServiceClient statementClient;

    @InjectMocks ReceiptService receiptService;

    private Receipt sampleReceipt;

    @BeforeEach
    void setUp() {
        sampleReceipt = Receipt.builder()
                .id(1L)
                .societyId(10L)
                .flatId(5L)
                .paymentId(100L)
                .receiptNumber("RCP-2024-001")
                .amount(new BigDecimal("5000.00"))
                .paymentMonth(3)
                .paymentYear(2024)
                .residentName("John Doe")
                .flatNumber("A-101")
                .societyName("Green Valley CHS")
                .build();
    }

    @Test
    @DisplayName("getReceiptsByFlat returns paged receipts for valid flat")
    void getReceiptsByFlat_returnsPagedReceipts() {
        var page = new PageImpl<>(List.of(sampleReceipt), PageRequest.of(0, 10), 1);
        when(receiptRepository.findBySocietyIdAndFlatId(eq(10L), eq(5L), any()))
                .thenReturn(page);

        var result = receiptService.getReceiptsByFlat(10L, 5L, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getReceiptNumber()).isEqualTo("RCP-2024-001");
        verify(receiptRepository).findBySocietyIdAndFlatId(eq(10L), eq(5L), any());
    }

    @Test
    @DisplayName("getReceiptsBySociety returns all receipts for a society")
    void getReceiptsBySociety_returnsAll() {
        var page = new PageImpl<>(List.of(sampleReceipt), PageRequest.of(0, 20), 1);
        when(receiptRepository.findBySocietyId(eq(10L), any())).thenReturn(page);

        var result = receiptService.getReceiptsBySociety(10L, 0, 20);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(receiptRepository).findBySocietyId(eq(10L), any());
    }
}
