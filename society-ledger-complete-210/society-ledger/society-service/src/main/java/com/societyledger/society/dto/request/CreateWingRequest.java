package com.societyledger.society.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWingRequest {
    @NotBlank String wingName;
    Integer totalFloors;
}
