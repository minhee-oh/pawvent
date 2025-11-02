package com.pawvent.pawventserver.dto;

import com.pawvent.pawventserver.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 사용자 정보 응답 DTO
 * 
 * 클라이언트에게 전달되는 사용자 정보를 담는 데이터 전송 객체입니다.
 * 민감한 정보(카카오 ID 등)는 제외하고 필요한 정보만을 포함합니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    /** 사용자 고유 식별자 */
    private Long id;
    
    /** 사용자 닉네임 */
    private String nickname;
    
    /** 사용자 이메일 주소 */
    private String email;
    
    /** 프로필 이미지 URL */
    private String profileImageUrl;
    
    /** 사용자 권한 (USER/ADMIN) */
    private Role role;
    
    /** 작성한 게시글 수 */
    private Integer postsCount;
    
    /** 계정 생성일 */
    private OffsetDateTime createdAt;
}

