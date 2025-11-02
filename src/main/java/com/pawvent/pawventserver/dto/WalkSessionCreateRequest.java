package com.pawvent.pawventserver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalkSessionCreateRequest {
    @NotNull
    private Long petId;
    
    private Long routeId; // 선택사항 - 기존 루트 사용한 경우
    private Double distance; // meters
    private Integer duration; // seconds
}






