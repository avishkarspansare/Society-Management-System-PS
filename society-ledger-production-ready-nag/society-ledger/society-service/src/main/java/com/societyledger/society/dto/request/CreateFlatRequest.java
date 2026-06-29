package com.societyledger.society.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateFlatRequest {
    @NotNull(message = "Wing ID is required")
    private Long wingId;

    @NotBlank(message = "Flat number is required")
    @Size(max = 20)
    private String flatNumber;

    private Integer floorNumber;
    private BigDecimal areaSqft;
    private Boolean isOccupied;
}
