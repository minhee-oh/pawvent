package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.dto.UserResponse;
import com.pawvent.pawventserver.dto.UserUpdateRequest;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 사용자 관리 컨트롤러
 * 
 * 사용자 정보 조회, 수정, 삭제 등의 기능을 제공합니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 현재 로그인한 사용자의 상세 정보를 조회합니다.
     * JWT 토큰을 통해 인증된 사용자의 프로필 정보를 반환합니다.
     * 
     * @param authentication Spring Security 인증 객체 (자동 주입)
     * @return 현재 사용자의 프로필 정보 (닉네임, 이메일, 프로필 이미지 등)
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        UserResponse userResponse = mapToUserResponse(currentUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("현재 사용자 정보를 조회했습니다.", userResponse)
        );
    }
    
    /**
     * 특정 사용자의 공개 프로필 정보를 조회합니다.
     * 다른 사용자의 프로필을 볼 때 사용되며, 민감한 정보는 제외됩니다.
     * 
     * @param userId 조회할 사용자의 고유 ID
     * @return 해당 사용자의 공개 프로필 정보
     * @throws IllegalArgumentException 존재하지 않는 사용자 ID인 경우
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        UserResponse userResponse = mapToUserResponse(user);
        
        return ResponseEntity.ok(
            ApiResponse.success("사용자 정보를 조회했습니다.", userResponse)
        );
    }
    
    /**
     * 현재 로그인한 사용자의 프로필을 수정합니다.
     * 닉네임과 프로필 이미지를 변경할 수 있으며, 닉네임 중복 검사가 자동으로 수행됩니다.
     * 
     * @param request 수정할 프로필 정보 (닉네임, 프로필 이미지 URL)
     * @param authentication 현재 인증된 사용자 정보
     * @return 수정된 사용자 프로필 정보
     * @throws IllegalArgumentException 닉네임이 이미 사용 중인 경우
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        User updatedUser = userService.updateUserProfile(
            currentUser.getId(),
            request.getNickname(),
            request.getProfileImageUrl(),
            currentUser
        );
        
        UserResponse userResponse = mapToUserResponse(updatedUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("프로필이 수정되었습니다.", userResponse)
        );
    }
    
    /**
     * 닉네임 중복 여부를 확인합니다.
     * 회원가입이나 프로필 수정 시 닉네임이 사용 가능한지 실시간으로 검증할 때 사용됩니다.
     * 
     * @param nickname 확인할 닉네임
     * @return true: 사용 가능, false: 이미 사용 중
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        
        return ResponseEntity.ok(
            ApiResponse.success(
                isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.",
                isAvailable
            )
        );
    }
    
    /**
     * 사용자 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String keyword) {
        List<User> users = userService.searchUsersByNickname(keyword);
        List<UserResponse> userResponses = users.stream()
                .map(this::mapToUserResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("사용자 검색이 완료되었습니다.", userResponses)
        );
    }
    
    /**
     * 모든 활성 사용자 조회 (관리자용)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.getAllActiveUsers(pageable);
        Page<UserResponse> userResponses = users.map(this::mapToUserResponse);
        
        return ResponseEntity.ok(
            ApiResponse.success("사용자 목록을 조회했습니다.", userResponses)
        );
    }
    
    /**
     * 계정 삭제 (탈퇴)
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        userService.deleteUser(currentUser.getId(), currentUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("계정이 삭제되었습니다.", null)
        );
    }
    
    /**
     * 사용자 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStats>> getUserStats() {
        long totalUsers = userService.getTotalUserCount();
        
        UserStats stats = UserStats.builder()
                .totalUsers(totalUsers)
                .build();
        
        return ResponseEntity.ok(
            ApiResponse.success("사용자 통계를 조회했습니다.", stats)
        );
    }
    
    /**
     * User 엔티티를 UserResponse DTO로 변환
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    /**
     * 사용자 통계 내부 클래스
     */
    public static class UserStats {
        private final Long totalUsers;
        
        private UserStats(Long totalUsers) {
            this.totalUsers = totalUsers;
        }
        
        public static UserStatsBuilder builder() {
            return new UserStatsBuilder();
        }
        
        public Long getTotalUsers() {
            return totalUsers;
        }
        
        public static class UserStatsBuilder {
            private Long totalUsers;
            
            public UserStatsBuilder totalUsers(Long totalUsers) {
                this.totalUsers = totalUsers;
                return this;
            }
            
            public UserStats build() {
                return new UserStats(totalUsers);
            }
        }
    }
}
