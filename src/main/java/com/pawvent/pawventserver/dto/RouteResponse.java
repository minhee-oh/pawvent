package com.pawvent.pawventserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteResponse {
    private Long id;
    private String name;
    private List<CoordinateDto> coordinates;
    private Double distance;
    private Integer duration;
    private boolean isShared;
    private Long authorId;
    private String authorNickname;
    private OffsetDateTime createdAt;
    
    @Data
    @AllArgsConstructor
    public static class CoordinateDto {
        private Double latitude;
        private Double longitude;
    }
}
