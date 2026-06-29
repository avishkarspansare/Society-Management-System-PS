package com.societyledger.finance.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateExpenseRequest {
    Long categoryId;
    @Positive BigDecimal amount;
    String vendorName;
    String description;
    LocalDate expenseDate;
}
