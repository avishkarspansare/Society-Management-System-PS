package com.societyledger.finance.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class CreateCategoryRequest {
    @NotBlank(message = "Category name is required") @Size(max = 100) private String name;
    private String description;
}
