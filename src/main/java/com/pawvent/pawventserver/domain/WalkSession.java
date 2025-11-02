package com.pawvent.pawventserver.domain;

import java.time.OffsetDateTime;

import org.locationtech.jts.geom.LineString;

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
 * 실제 산책 세션 엔티티
 * 
 * 사용자가 반려동물과 함께 진행한 실제 산책 기록을 저장합니다.
 * GPS 추적을 통한 실제 이동 경로와 거리, 시간 등의 데이터를 기록하며,
 * 산책 통계 및 건강 관리의 기초 데이터로 활용됩니다.
 * 미리 계획된 루트를 참조하거나 자유 산책도 가능합니다.
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
@Table(name = "walk_session", 
    indexes = {
        @Index(name = "ix_walk_session_user", columnList = "user_id"),
        @Index(name = "ix_walk_session_pet", columnList = "pet_id"),
        @Index(name = "ix_walk_session_start_time", columnList = "start_time")
    }
)
public class WalkSession extends BaseTime {

    /** 산책 세션 고유 식별자 (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 산책 시작 시간 */
    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    /** 산책 종료 시간 (진행 중인 경우 null) */
    @Column(name = "end_time")
    private OffsetDateTime endTime;

    /** GPS 추적으로 기록된 실제 이동 경로 (LineString 형태의 지리정보) */
    @Column(name = "actual_path", columnDefinition = "geometry(LineString,4326)")
    private LineString actualPath;

    /** 실제 산책 거리 (미터 단위) */
    @Column(name = "actual_distance")
    private Double actualDistance;

    /** 실제 산책 소요시간 (초 단위) */
    @Column(name = "actual_duration")
    private Integer actualDuration;

    /** 세션 완료 여부 (true: 완료, false: 진행 중 또는 중단) */
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;
    
    /** 세션 삭제 시간 (소프트 삭제) */
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    /** 산책을 진행한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 산책에 참여한 반려동물 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    /** 참조한 계획 루트 (자유 산책인 경우 null) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private WalkRoute route;
}
