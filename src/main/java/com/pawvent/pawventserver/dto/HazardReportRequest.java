package com.pawvent.pawventserver.dto;

import com.pawvent.pawventserver.domain.enums.HazardCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HazardReportRequest {
    @NotNull
    private HazardCategory category;
    
    private String description;
    
    @NotNull
    private Double latitude;
    
    @NotNull
    private Double longitude;
    
    private String imageUrl;
}
