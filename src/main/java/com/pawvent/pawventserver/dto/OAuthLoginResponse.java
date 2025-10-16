package com.pawvent.pawventserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OAuthLoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserResponse user;
    private boolean isNewUser; // 신규 가입 여부
}

