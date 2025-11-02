package com.pawvent.pawventserver.domain;

import java.time.LocalDate;

import com.pawvent.pawventserver.domain.common.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 챌린지 정보 엔티티
 * 
 * 반려동물과 함께하는 다양한 챌린지 정보를 관리합니다.
 * 기간이 정해진 목표 달성 활동으로, 사용자들의 참여를 유도합니다.
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
@Entity 
@Table(name = "challenge")
public class Challenge extends BaseTime {

    /** 챌린지 고유 식별자 (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 챌린지 제목 */
    @Column(length = 120, nullable = false)
    private String title;

    /** 챌린지 상세 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 챌린지 목표 값 (예: 30 -> 30분, 10000 -> 10000보) */
    @Column(name = "target_value")
    private Integer targetValue;

    /** 챌린지 이미지 URL */
    @Column(name = "image_url")
    private String imageUrl;

    /** 챌린지 활성화 여부 */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** 챌린지 시작일 */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** 챌린지 종료일 */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** 챌린지 삭제 시간 (소프트 삭제를 위한 필드, null이면 활성 챌린지) */
    @Column(name = "deleted_at")
    private java.time.OffsetDateTime deletedAt;
}
