package com.societyledger.society.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateFlatRequest {
    @NotNull Long wingId;
    @NotBlank String flatNumber;
    Integer floorNumber;
    BigDecimal areaSqft;
    Boolean isOccupied;
}
