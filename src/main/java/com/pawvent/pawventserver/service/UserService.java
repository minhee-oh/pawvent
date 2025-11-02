package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.Role;
import com.pawvent.pawventserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * 인증 정보를 통해 현재 로그인된 사용자를 조회합니다.
     * JWT 토큰의 userId를 사용하여 사용자를 찾습니다.
     * 
     * @param authentication Spring Security의 인증 객체
     * @return 현재 로그인된 사용자 엔티티
     * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
     */
    public User getCurrentUser(Authentication authentication) {
        // 인증이 없거나 anonymous인 경우 더미 사용자 반환 (개발용)
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal().toString())) {
            // 개발용: 첫 번째 사용자를 반환하거나 더미 사용자 생성
            return getOrCreateDummyUser();
        }
        
        try {
            // JWT 필터에서 설정한 userId (principal이 Long 타입)
            Long userId = Long.valueOf(authentication.getPrincipal().toString());
            return getUserById(userId);
        } catch (NumberFormatException | ClassCastException e) {
            // JWT가 아닌 경우 (OAuth2 등) 처리
            try {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                Long kakaoId = oauth2User.getAttribute("id");
                
                return userRepository.findByKakaoId(kakaoId)
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            } catch (Exception ex) {
                // 개발용: 더미 사용자 반환
                return getOrCreateDummyUser();
            }
        }
    }
    
    /**
     * 더미 사용자를 조회하거나 생성합니다 (개발용)
     * INSERT를 수행하므로 readOnly=false로 설정
     */
    @Transactional(readOnly = false)
    private User getOrCreateDummyUser() {
        return userRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    // 사용자가 없으면 더미 사용자 생성
                    User dummyUser = User.builder()
                            .email("dummy@example.com")
                            .nickname("테스트 사용자")
                            .role(Role.USER)
                            .kakaoId(999999L)
                            .build();
                    return userRepository.save(dummyUser);
                });
    }
    
    /**
     * 사용자 ID로 특정 사용자를 조회합니다.
     * 
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자 엔티티
     * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
    
    /**
     * 카카오 ID로 사용자를 조회합니다.
     * OAuth 로그인 처리 시 사용됩니다.
     * 
     * @param kakaoId 카카오 사용자 ID
     * @return 사용자 엔티티 (Optional)
     */
    public Optional<User> getUserByKakaoId(Long kakaoId) {
        return userRepository.findByKakaoId(kakaoId);
    }
    
    /**
     * 닉네임으로 사용자를 조회합니다.
     * 닉네임 중복 확인 등에 사용됩니다.
     * 
     * @param nickname 조회할 닉네임
     * @return 사용자 엔티티 (Optional)
     */
    public Optional<User> getUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }
    
    /**
     * 이메일로 사용자를 조회합니다.
     * 
     * @param email 조회할 이메일 주소
     * @return 사용자 엔티티 (Optional)
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Transactional
    public User updateUserProfile(Long userId, String nickname, String profileImageUrl, User requestUser) {
        User user = getUserById(userId);
        
        if (!user.getId().equals(requestUser.getId())) {
            throw new IllegalArgumentException("프로필을 수정할 권한이 없습니다.");
        }
        
        // 닉네임 중복 검사 (자신 제외)
        if (!user.getNickname().equals(nickname)) {
            Optional<User> existingUser = getUserByNickname(nickname);
            if (existingUser.isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        }
        
        user.setNickname(nickname);
        user.setProfileImageUrl(profileImageUrl);
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateNickname(Long userId, String nickname, User requestUser) {
        User user = getUserById(userId);
        
        if (!user.getId().equals(requestUser.getId())) {
            throw new IllegalArgumentException("닉네임을 수정할 권한이 없습니다.");
        }
        
        // 닉네임 중복 검사 (자신 제외)
        if (!user.getNickname().equals(nickname)) {
            Optional<User> existingUser = getUserByNickname(nickname);
            if (existingUser.isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        }
        
        user.setNickname(nickname);
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateProfileImage(Long userId, String profileImageUrl, User requestUser) {
        User user = getUserById(userId);
        
        if (!user.getId().equals(requestUser.getId())) {
            throw new IllegalArgumentException("프로필 이미지를 수정할 권한이 없습니다.");
        }
        
        user.setProfileImageUrl(profileImageUrl);
        return userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long userId, User requestUser) {
        User user = getUserById(userId);
        
        if (!user.getId().equals(requestUser.getId()) && requestUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("계정을 삭제할 권한이 없습니다.");
        }
        
        user.setDeletedAt(OffsetDateTime.now());
        userRepository.save(user);
    }
    
    public boolean isNicknameAvailable(String nickname) {
        return getUserByNickname(nickname).isEmpty();
    }
    
    public boolean isEmailTaken(String email) {
        return getUserByEmail(email).isPresent();
    }
    
    public List<User> getAllActiveUsers() {
        return userRepository.findByDeletedAtIsNull();
    }
    
    public Page<User> getAllActiveUsers(Pageable pageable) {
        return userRepository.findByDeletedAtIsNull(pageable);
    }
    
    public List<User> searchUsersByNickname(String nickname) {
        return userRepository.findByNicknameContainingAndDeletedAtIsNull(nickname);
    }
    
    public long getTotalUserCount() {
        return userRepository.countByDeletedAtIsNull();
    }
    
    @Transactional
    public User promoteToAdmin(Long userId) {
        User user = getUserById(userId);
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }
    
    @Transactional
    public User demoteToUser(Long userId) {
        User user = getUserById(userId);
        user.setRole(Role.USER);
        return userRepository.save(user);
    }
    
    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }
    
    public boolean isUserOwner(Long userId, User requestUser) {
        return userId.equals(requestUser.getId());
    }
    
    /**
     * 사용자의 활동 통계 조회를 위한 메서드들
     */
    public boolean isUserActive(User user) {
        return user.getDeletedAt() == null;
    }
    
    /**
     * 중복 닉네임 검사 (현재 사용자 제외)
     */
    public boolean isNicknameAvailableForUser(String nickname, Long currentUserId) {
        Optional<User> existingUser = getUserByNickname(nickname);
        return existingUser.isEmpty() || existingUser.get().getId().equals(currentUserId);
    }
}

