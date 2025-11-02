package com.pawvent.pawventserver.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PetCreateRequest {
    @NotBlank
    @Size(min = 1, max = 60)
    private String name;
    
    @Size(max = 40)
    private String species; // 개, 고양이 등
    
    @Size(max = 80)
    private String breed;
    
    private LocalDate birthDate;
    
    @Size(max = 10)
    private String gender; // 남, 여, 중성
    
    @Min(0)
    private Double weight; // kg
    
    private String imageUrl;
    
    private String description;
}




