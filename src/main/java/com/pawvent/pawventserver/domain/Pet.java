package com.pawvent.pawventserver.domain;

import com.pawvent.pawventserver.domain.common.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 반려동물 프로필 엔티티
 * 
 * 사용자가 등록한 반려동물의 기본 정보를 저장하고 관리합니다.
 * 산책 기록, 건강 관리, 챌린지 참여 등 반려동물 관련 모든 기능의 
 * 기준이 되는 핵심 엔티티입니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@Entity @Table(name = "pet", indexes = { @Index(name = "ix_pet_user", columnList = "user_id") })
public class Pet extends BaseTime {

    /** 반려동물 고유 식별자 (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 반려동물 이름 (사용자가 지정한 애칭) */
    @Column(length = 60, nullable = false)
    private String name;

    /** 동물 종류 (개, 고양이 등) */
    @Column(length = 40)
    private String species;

    /** 반려동물 품종 (예: 골든 리트리버, 믹스견 등) */
    @Column(length = 80)
    private String breed;

    /** 반려동물 생년월일 */
    @Column(name = "birth_date")
    private java.time.LocalDate birthDate;

    /** 반려동물 성별 */
    @Column(length = 10)
    private String gender;

    /** 반려동물 체중 (kg) */
    @Column
    private Double weight;

    /** 반려동물 프로필 이미지 URL */
    @Column(name = "image_url")
    private String imageUrl;

    /** 반려동물 설명 (특이사항, 소개 등) */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 삭제 시간 (소프트 삭제) */
    @Column(name = "deleted_at")
    private java.time.OffsetDateTime deletedAt;

    /** 반려동물 소유자 (등록한 사용자와의 관계) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
