package com.pawvent.pawventserver.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PetCreateRequest {
    @NotBlank
    @Size(min = 1, max = 60)
    private String name;
    
    @Size(max = 80)
    private String breed;
    
    @Min(0)
    private Integer age;
    
    private String imageUrl;
}

