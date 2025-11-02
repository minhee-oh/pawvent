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
import org.locationtech.jts.geom.LineString;

/**
 * 산책 경로 엔티티
 * 
 * 사용자가 계획하거나 기록한 산책 경로 정보를 저장합니다.
 * GPS 좌표를 기반으로 한 LineString 형태의 지리정보와
 * 거리, 소요시간 등의 메타데이터를 포함합니다.
 * 다른 사용자와 공유 가능한 루트로 설정할 수 있습니다.
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
@Table(name = "walk_route", indexes = { @Index(name = "ix_walk_route_user", columnList = "user_id") })
public class WalkRoute extends BaseTime {

    /** 산책 경로 고유 식별자 (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 산책 경로 이름 (사용자가 지정한 루트명) */
    @Column(length = 80, nullable = false)
    private String name;

    /** GPS 좌표 기반 경로 데이터 (LineString 형태의 지리정보, SRID 4326) */
    @Column(name = "route_data", columnDefinition = "geometry(LineString,4326)", nullable = false)
    private LineString routeData;

    /** 경로 총 거리 (미터 단위) */
    @Column
    private Double distance;

    /** 예상 소요시간 (초 단위) */
    @Column
    private Integer duration;

    /** 공개 여부 (true: 다른 사용자에게 공개, false: 비공개) */
    @Column(name = "is_shared", nullable = false)
    private boolean isShared;

    /** 경로 작성자 (경로를 등록한 사용자) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
