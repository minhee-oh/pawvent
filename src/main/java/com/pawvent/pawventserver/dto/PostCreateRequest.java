package com.pawvent.pawventserver.dto;

import com.pawvent.pawventserver.domain.enums.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostCreateRequest {
    @NotBlank
    @Size(min = 1, max = 200)
    private String title;
    
    private String content;
    
    @NotNull
    private PostCategory category;
}

