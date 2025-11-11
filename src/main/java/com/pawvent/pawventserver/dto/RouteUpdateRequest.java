package com.pawvent.pawventserver.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RouteUpdateRequest {
    @NotNull
    @Size(min = 1, max = 80)
    private String name;
    
    @NotNull
    private List<CoordinateDto> coordinates;
    
    private Double distance;
    private Integer duration;
    private boolean isShared = false;
    
    @Data
    public static class CoordinateDto {
        @NotNull
        private Double latitude;
        
        @NotNull  
        private Double longitude;
    }
}











