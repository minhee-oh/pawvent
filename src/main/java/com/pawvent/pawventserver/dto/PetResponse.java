package com.pawvent.pawventserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 반려동물 정보 응답 DTO
 * 
 * 클라이언트에게 전달되는 반려동물 정보를 담는 데이터 전송 객체입니다.
 * 반려동물 관리, 산책 기록 등에서 활용됩니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@AllArgsConstructor
public class PetResponse {
    /** 반려동물 고유 식별자 */
    private Long id;
    
    /** 반려동물 이름 */
    private String name;
    
    /** 동물 종류 (개, 고양이 등) */
    private String species;
    
    /** 반려동물 품종 */
    private String breed;
    
    /** 반려동물 생년월일 */
    private LocalDate birthDate;
    
    /** 반려동물 성별 */
    private String gender;
    
    /** 반려동물 체중 (kg) */
    private Double weight;
    
    /** 반려동물 프로필 이미지 URL */
    private String imageUrl;
    
    /** 반려동물 설명 (특이사항, 소개 등) */
    private String description;
    
    /** 소유자 ID */
    private Long ownerId;
    
    /** 소유자 닉네임 */
    private String ownerNickname;
    
    /** 등록일 */
    private OffsetDateTime createdAt;
}

