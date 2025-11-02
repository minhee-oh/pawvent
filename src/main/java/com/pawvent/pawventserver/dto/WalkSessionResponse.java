package com.pawvent.pawventserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 산책 세션 응답 DTO
 * 
 * 클라이언트에게 전달되는 산책 세션 정보를 담는 데이터 전송 객체입니다.
 * 산책 기록 조회, 통계 표시 등에서 활용됩니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalkSessionResponse {
    /** 산책 세션 고유 식별자 */
    private Long id;
    
    /** 사용자 ID */
    private Long userId;
    
    /** 사용자 닉네임 */
    private String userNickname;
    
    /** 산책에 참여한 반려동물 ID */
    private Long petId;
    
    /** 산책에 참여한 반려동물 이름 */
    private String petName;
    
    /** 사용한 산책 루트 ID (자유 산책인 경우 null) */
    private Long routeId;
    
    /** 사용한 산책 루트 이름 */
    private String routeName;
    
    /** 실제 산책 거리 (미터 단위) */
    private Double distance;
    
    /** 실제 산책 시간 (초 단위) */
    private Integer duration;
    
    /** 산책 시작 시간 */
    private OffsetDateTime startTime;
    
    /** 산책 종료 시간 */
    private OffsetDateTime endTime;
    
    /** 세션 완료 여부 */
    private Boolean isCompleted;
    
    /** 생성 시각 */
    private OffsetDateTime createdAt;
}
