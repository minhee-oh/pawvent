package com.pawvent.pawventserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank
    @Size(min = 2, max = 40)
    private String nickname;
    
    private String profileImageUrl;
}




