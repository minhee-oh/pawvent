package com.pawvent.pawventserver.domain;

import org.locationtech.jts.geom.Point;
import java.time.OffsetDateTime;

import com.pawvent.pawventserver.domain.common.BaseTime;
import com.pawvent.pawventserver.domain.enums.HazardCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 위험 스팟 정보 엔티티
 * 
 * 산책로 상의 위험한 장소나 주의사항을 기록하는 엔티티입니다.
 * 사용자들이 발견한 위험 요소를 GPS 좌표와 함께 공유하여
 * 다른 사용자들이 안전한 산책을 할 수 있도록 도움을 줍니다.
 * 카테고리별로 분류되며, 지리적 검색이 가능합니다.
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
@Table(name = "hazard", indexes = {@Index(name = "ix_hazard_user", columnList = "user_id"),@Index(name = "ix_hazard_category_created", columnList = "category, created_at")})
public class Hazard extends BaseTime {

    /** 위험 스팟 고유 식별자 (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 위험 요소 카테고리 (도로 위험, 환경 위험, 시설물 위험 등) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private HazardCategory category;

    /** 위험 요소에 대한 상세 설명 (사용자가 작성한 설명) */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 위험 스팟의 GPS 좌표 (Point 형태의 지리정보, SRID 4326) */
    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    /** 위험 스팟 관련 이미지 URL (현장 사진) */
    @Column(name = "image_url")
    private String imageUrl;

    /** 위험 스팟을 신고한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 위험 스팟 삭제 시간 (소프트 삭제를 위한 필드, null이면 활성 스팟) */
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
