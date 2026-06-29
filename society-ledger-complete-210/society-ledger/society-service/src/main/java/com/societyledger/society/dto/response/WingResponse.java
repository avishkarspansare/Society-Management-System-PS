package com.societyledger.society.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class WingResponse {
    Long id;
    Long societyId;
    String wingName;
    Integer totalFloors;
    int flatCount;
}
