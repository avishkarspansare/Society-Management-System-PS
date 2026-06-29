package com.societyledger.finance.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class CreateExpenseRequest {
    @NotNull(message = "Category is required") private Long categoryId;
    @NotNull(message = "Amount is required") @DecimalMin("0.01") private BigDecimal amount;
    @NotBlank(message = "Vendor name is required") @Size(max = 255) private String vendorName;
    @NotBlank(message = "Description is required") private String description;
    @NotNull(message = "Expense date is required") private LocalDate expenseDate;
}
