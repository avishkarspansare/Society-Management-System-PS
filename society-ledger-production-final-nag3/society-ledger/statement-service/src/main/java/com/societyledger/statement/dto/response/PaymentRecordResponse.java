package com.societyledger.statement.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentRecordResponse {
    private Long id;
    private Long societyId;
    private Long flatId;
    private Integer paymentMonth;
    private Integer paymentYear;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentReference;
    private String matchType;
    private Instant createdAt;
}
