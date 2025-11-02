package com.pawvent.pawventserver.domain;

import java.time.OffsetDateTime;

import com.pawvent.pawventserver.domain.common.BaseTime;
import com.pawvent.pawventserver.domain.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 정보 엔티티
 * 
 * Pawvent 서비스를 이용하는 사용자의 기본 정보를 관리합니다.
 * 카카오 소셜 로그인을 통해 가입한 사용자 정보를 저장하며,
 * 반려동물 관리, 산책 기록, 커뮤니티 활동 등의 기능을 제공합니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@Getter @Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@Entity 
@Table(name = "users",indexes = {@Index(name = "ix_users_nickname", columnList = "nickname")})
public class User extends BaseTime {

    /** 사용자 고유 식별자 (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카카오 로그인 사용자 ID (카카오 API에서 제공하는 고유 ID) */
    @Column(name = "kakao_id", unique = true)
    private Long kakaoId;

    /** 사용자 이메일 주소 (카카오 계정 이메일) */
    @Column(length = 100)
    private String email;

    /** 사용자 닉네임 (서비스 내에서 표시되는 이름, 중복 불가) */
    @Column(length = 40, nullable = false)
    private String nickname;

    /** 프로필 이미지 URL (카카오 프로필 이미지 또는 사용자 업로드 이미지) */
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    /** 사용자 권한 (USER: 일반 사용자, ADMIN: 관리자) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** 계정 삭제 시간 (소프트 삭제를 위한 필드, null이면 활성 계정) */
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
