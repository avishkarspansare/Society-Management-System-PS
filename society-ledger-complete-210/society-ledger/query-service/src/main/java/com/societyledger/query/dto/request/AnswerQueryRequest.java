package com.societyledger.query.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnswerQueryRequest {
    @NotBlank String answer;
}
