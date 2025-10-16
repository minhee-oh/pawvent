package com.pawvent.pawventserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
public class WalkSessionResponse {
    private Long id;
    private Long petId;
    private String petName;
    private Long routeId; // 사용한 루트 (있는 경우)
    private String routeName;
    private Double distance;
    private Integer duration;
    private OffsetDateTime createdAt;
}
