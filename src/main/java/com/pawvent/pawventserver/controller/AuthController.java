package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.Role;
import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.dto.LoginResponse;
import com.pawvent.pawventserver.repository.UserRepository;
import com.pawvent.pawventserver.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient webClient;

    /**
     * 헬스 체크 엔드포인트 (테스트용)
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        log.info("✅ /api/auth/test 엔드포인트 호출됨!");
        return ResponseEntity.ok(ApiResponse.success("AuthController가 정상 작동합니다.", "OK"));
    }

    /**
     * 카카오 액세스 토큰으로 로그인 처리 및 JWT 토큰 발급
     */
    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(@RequestBody Map<String, String> request) {
        try {
            String accessToken = request.get("accessToken");
            if (accessToken == null || accessToken.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("카카오 액세스 토큰이 필요합니다."));
            }

            // 카카오 API로 사용자 정보 조회
            Map<String, Object> kakaoUserInfo = getKakaoUserInfo(accessToken);
            if (kakaoUserInfo == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("카카오 사용자 정보를 가져올 수 없습니다."));
            }

            Long kakaoId = Long.valueOf(String.valueOf(kakaoUserInfo.get("id")));
            Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            
            String email = (String) kakaoAccount.get("email");
            String nickname = (String) profile.get("nickname");
            String profileImageUrl = (String) profile.get("profile_image_url");

            // 사용자 조회 또는 생성
            User user = getUserOrCreate(kakaoId, email, nickname, profileImageUrl);

            // JWT 토큰 생성
            String jwtToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

            LoginResponse loginResponse = LoginResponse.builder()
                    .token(jwtToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("로그인 성공", loginResponse));

        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류 발생", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("로그인 처리에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 카카오 API로 사용자 정보 조회
     */
    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            return null;
        }
    }

    /**
     * 사용자 조회 또는 생성
     */
    @Transactional
    private User getUserOrCreate(Long kakaoId, String email, String nickname, String profileImageUrl) {
        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // 프로필 정보 업데이트
            user.setNickname(nickname);
            user.setProfileImageUrl(profileImageUrl);
            return userRepository.save(user);
        } else {
            // 새 사용자 생성
            User newUser = User.builder()
                    .kakaoId(kakaoId)
                    .email(email != null ? email : "kakao_" + kakaoId + "@kakao.com")
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .role(Role.USER)
                    .build();
            return userRepository.save(newUser);
        }
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            org.springframework.security.core.Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증되지 않은 사용자입니다."));
            }

            Long userId = Long.valueOf(authentication.getPrincipal().toString());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Map<String, Object> userInfo = Map.of(
                    "id", user.getId(),
                    "email", user.getEmail() != null ? user.getEmail() : "",
                    "nickname", user.getNickname() != null ? user.getNickname() : "",
                    "profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : ""
            );

            return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 성공", userInfo));
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("사용자 정보 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}

