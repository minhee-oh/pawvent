package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.Role;
import com.pawvent.pawventserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        if ("kakao".equals(registrationId)) {
            return processKakaoUser(oauth2User);
        }
        
        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
    }

    private OAuth2User processKakaoUser(OAuth2User oauth2User) {
        Long kakaoId = oauth2User.getAttribute("id");
        Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        
        String email = (String) kakaoAccount.get("email");
        String nickname = (String) profile.get("nickname");
        String profileImageUrl = (String) profile.get("profile_image_url");

        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // 프로필 정보 업데이트
            user.setNickname(nickname);
            user.setProfileImageUrl(profileImageUrl);
        } else {
            // 새 사용자 생성
            user = User.builder()
                    .kakaoId(kakaoId)
                    .email(email)
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .role(Role.USER)
                    .build();
        }
        
        userRepository.save(user);
        
        return oauth2User;
    }
}
