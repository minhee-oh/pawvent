package com.pawvent.pawventserver.domain;

import java.time.OffsetDateTime;

import com.pawvent.pawventserver.domain.common.BaseTime;
import com.pawvent.pawventserver.domain.enums.NotificationType;

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
 * 알림 엔티티
 * 
 * 사용자에게 전송되는 각종 알림을 관리합니다.
 * 댓글, 좋아요, 시스템 공지 등 다양한 타입의 알림을 지원하며,
 * 다형성 연관관계를 통해 관련된 엔티티 정보를 저장합니다.
 * 읽음/안읽음 상태를 추적하여 사용자 경험을 개선합니다.
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
@Table(name = "notification", 
    indexes = {
        @Index(name = "ix_notification_user", columnList = "user_id"),
        @Index(name = "ix_notification_type", columnList = "type"),
        @Index(name = "ix_notification_read", columnList = "is_read"),
        @Index(name = "ix_notification_user_read_created", columnList = "user_id, is_read, created_at")
    }
)
public class Notification extends BaseTime {

    /** 알림 고유 식별자 (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 알림 타입 (댓글, 좋아요, 시스템 공지 등) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    /** 알림 제목 */
    @Column(length = 200, nullable = false)
    private String title;

    /** 알림 상세 메시지 */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    /** 알림 클릭 시 이동할 URL */
    @Column(length = 500)
    private String url;

    /** 읽음 상태 (true: 읽음, false: 안읽음) */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /** 알림을 읽은 시간 */
    @Column(name = "read_at")
    private OffsetDateTime readAt;

    /** 관련된 엔티티의 타입 (다형성 연관관계를 위한 필드) */
    @Column(name = "related_entity_type")
    private String relatedEntityType; // "CommunityPost", "Comment", "Hazard", etc.

    /** 관련된 엔티티의 ID (다형성 연관관계를 위한 필드) */
    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    /** 알림 삭제 시간 (소프트 삭제) */
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    /** 알림을 받을 사용자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 알림을 읽음으로 표시하는 메서드
     * 읽음 상태를 true로 변경하고 읽은 시간을 현재 시간으로 설정합니다.
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = OffsetDateTime.now();
    }
}

