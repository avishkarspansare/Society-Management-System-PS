package com.societyledger.statement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Normalised representation of a single bank transaction row,
 * regardless of which bank it came from.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedTransaction {
    private LocalDate transactionDate;
    private LocalDate valueDate;
    private String description;
    private String referenceNumber;
    private BigDecimal creditAmount;
    private BigDecimal debitAmount;
    private BigDecimal balance;
    private String rawLine;  // original row for debugging
}
