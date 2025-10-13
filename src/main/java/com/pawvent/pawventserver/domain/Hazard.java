package com.pawvent.pawventserver.domain;

import java.awt.Point;
import java.time.OffsetDateTime;

import org.hibernate.annotations.BatchSize;

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

//위험 스팟 정보 엔티티
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@EqualsAndHashCode(of = "id")
@Entity 
@Table(name = "hazard", indexes = {@Index(name = "ix_hazard_user", columnList = "user_id"),@Index(name = "ix_hazard_category_created", columnList = "category, created_at")})
public class Hazard extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private HazardCategory category;

    @BatchSize(size = 500)
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 소프트 삭제
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
