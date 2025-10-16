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

//알림 엔티티
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    // 관련 엔티티 참조 (polymorphic association)
    @Column(name = "related_entity_type")
    private String relatedEntityType; // "CommunityPost", "Comment", "Hazard", etc.

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 알림 읽음 처리
    public void markAsRead() {
        this.isRead = true;
        this.readAt = OffsetDateTime.now();
    }
}
