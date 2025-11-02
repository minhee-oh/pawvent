package com.pawvent.pawventserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotNull
    private Long postId;
    
    @NotBlank
    private String content;
}




