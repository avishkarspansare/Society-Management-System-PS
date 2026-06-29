package com.societyledger.query.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateQueryRequest {
    @NotBlank String subject;
    @NotBlank String body;
}
