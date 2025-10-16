package com.pawvent.pawventserver.dto;

import com.pawvent.pawventserver.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String nickname;
    private String email;
    private String profileImageUrl;
    private Role role;
    private Integer postsCount;
    private OffsetDateTime createdAt;
}

