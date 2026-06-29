package com.societyledger.finance.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateExpenseRequest {

    @Min(value = 1, message = "Category ID must be valid")
    private Long categoryId;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Size(max = 255, message = "Vendor name must be at most 255 characters")
    private String vendorName;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @PastOrPresent(message = "Expense date cannot be in the future")
    private LocalDate expenseDate;
}
