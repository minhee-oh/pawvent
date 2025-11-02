package com.pawvent.pawventserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageRequest {
    @Min(0)
    private int page = 0;
    
    @Min(1)
    @Max(100)
    private int size = 10;
    
    private String sort = "createdAt";
    private String direction = "DESC";
}






