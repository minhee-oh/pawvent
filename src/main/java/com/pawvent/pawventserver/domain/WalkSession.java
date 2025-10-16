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

//실제 산책 세션 엔티티
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    // 실제 이동 경로 (GPS 추적)
    @Column(name = "actual_path", columnDefinition = "geometry(LineString,4326)")
    private LineString actualPath;

    @Column(name = "actual_distance") // meters
    private Double actualDistance;

    @Column(name = "actual_duration") // seconds  
    private Integer actualDuration;

    // 세션 완료 여부
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    // 참조한 루트 (선택사항)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private WalkRoute route;
}
