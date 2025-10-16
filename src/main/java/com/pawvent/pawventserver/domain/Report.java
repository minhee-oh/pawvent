package com.pawvent.pawventserver.domain;

import java.time.OffsetDateTime;

import com.pawvent.pawventserver.domain.common.BaseTime;
import com.pawvent.pawventserver.domain.enums.ReportStatus;
import com.pawvent.pawventserver.domain.enums.ReportType;

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

//신고 엔티티
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@EqualsAndHashCode(of = "id")
@Entity 
@Table(name = "report", 
    indexes = {
        @Index(name = "ix_report_reporter", columnList = "reporter_id"),
        @Index(name = "ix_report_status", columnList = "status"),
        @Index(name = "ix_report_type", columnList = "type"),
        @Index(name = "ix_report_target", columnList = "target_entity_type, target_entity_id"),
        @Index(name = "ix_report_status_created", columnList = "status, created_at")
    }
)
public class Report extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    // 신고 대상 엔티티 (polymorphic association)
    @Column(name = "target_entity_type", nullable = false, length = 50)
    private String targetEntityType; // "CommunityPost", "Comment", "Hazard", "User" etc.

    @Column(name = "target_entity_id", nullable = false)
    private Long targetEntityId;

    // 관리자 응답
    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    // 처리한 관리자 (선택사항)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    // 신고 처리
    public void resolve(String adminResponse, User admin) {
        this.status = ReportStatus.RESOLVED;
        this.adminResponse = adminResponse;
        this.processedBy = admin;
        this.processedAt = OffsetDateTime.now();
    }

    // 신고 반려
    public void reject(String adminResponse, User admin) {
        this.status = ReportStatus.REJECTED;
        this.adminResponse = adminResponse;
        this.processedBy = admin;
        this.processedAt = OffsetDateTime.now();
    }

    // 검토 시작
    public void startReview() {
        this.status = ReportStatus.REVIEWING;
    }
}
