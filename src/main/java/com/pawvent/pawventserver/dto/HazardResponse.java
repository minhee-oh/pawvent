package com.pawvent.pawventserver.dto;

import com.pawvent.pawventserver.domain.enums.HazardCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 위험 스팟 응답 DTO
 * 
 * 클라이언트에게 전달되는 위험 스팟 정보를 담는 데이터 전송 객체입니다.
 * GPS 좌표는 위도/경도로 분리하여 클라이언트에서 쉽게 활용할 수 있도록 합니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@AllArgsConstructor
public class HazardResponse {
    /** 위험 스팟 고유 식별자 */
    private Long id;
    
    /** 위험 요소 카테고리 */
    private HazardCategory category;
    
    /** 위험 요소 설명 */
    private String description;
    
    /** GPS 위도 좌표 */
    private Double latitude;
    
    /** GPS 경도 좌표 */
    private Double longitude;
    
    /** 위험 스팟 이미지 URL */
    private String imageUrl;
    
    /** 신고자 ID */
    private Long reporterId;
    
    /** 신고자 닉네임 */
    private String reporterNickname;
    
    /** 신고일 */
    private OffsetDateTime createdAt;
}
