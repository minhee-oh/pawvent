package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.domain.Notification;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.NotificationType;
import com.pawvent.pawventserver.service.NotificationService;
import com.pawvent.pawventserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 알림 관리 컨트롤러
 * 
 * 사용자 알림의 조회, 읽음 처리, 삭제 등의 기능을 제공합니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    
    /**
     * 내 알림 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            Pageable pageable,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Page<Notification> notifications = notificationService.getUserNotifications(currentUser, pageable);
        Page<NotificationResponse> notificationResponses = notifications.map(this::mapToNotificationResponse);
        
        return ResponseEntity.ok(
            ApiResponse.success("내 알림을 조회했습니다.", notificationResponses)
        );
    }
    
    /**
     * 읽지 않은 알림 조회
     */
    @GetMapping("/my/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        List<Notification> notifications = notificationService.getUnreadNotifications(currentUser);
        List<NotificationResponse> notificationResponses = notifications.stream()
                .map(this::mapToNotificationResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("읽지 않은 알림을 조회했습니다.", notificationResponses)
        );
    }
    
    /**
     * 타입별 알림 조회
     */
    @GetMapping("/my/type/{type}")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotificationsByType(
            @PathVariable NotificationType type,
            Pageable pageable,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Page<Notification> notifications = notificationService.getNotificationsByType(currentUser, type, pageable);
        Page<NotificationResponse> notificationResponses = notifications.map(this::mapToNotificationResponse);
        
        return ResponseEntity.ok(
            ApiResponse.success(type + " 타입의 알림을 조회했습니다.", notificationResponses)
        );
    }
    
    /**
     * 특정 알림 조회
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(@PathVariable Long notificationId) {
        Notification notification = notificationService.getNotificationById(notificationId);
        NotificationResponse notificationResponse = mapToNotificationResponse(notification);
        
        return ResponseEntity.ok(
            ApiResponse.success("알림을 조회했습니다.", notificationResponse)
        );
    }
    
    /**
     * 알림 읽음 처리
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Notification notification = notificationService.markAsRead(notificationId, currentUser);
        NotificationResponse notificationResponse = mapToNotificationResponse(notification);
        
        return ResponseEntity.ok(
            ApiResponse.success("알림을 읽음으로 처리했습니다.", notificationResponse)
        );
    }
    
    /**
     * 모든 알림 읽음 처리
     */
    @PostMapping("/my/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        notificationService.markAllAsRead(currentUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("모든 알림을 읽음으로 처리했습니다.", null)
        );
    }
    
    /**
     * 특정 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long notificationId,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        notificationService.deleteNotification(notificationId, currentUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("알림이 삭제되었습니다.", null)
        );
    }
    
    /**
     * 모든 알림 삭제
     */
    @DeleteMapping("/my/all")
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        notificationService.deleteAllNotifications(currentUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("모든 알림이 삭제되었습니다.", null)
        );
    }
    
    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/my/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        long unreadCount = notificationService.getUnreadNotificationCount(currentUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("읽지 않은 알림 개수를 조회했습니다.", unreadCount)
        );
    }
    
    /**
     * 총 알림 개수 조회
     */
    @GetMapping("/my/count")
    public ResponseEntity<ApiResponse<Long>> getTotalNotificationCount(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        long totalCount = notificationService.getTotalNotificationCount(currentUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("총 알림 개수를 조회했습니다.", totalCount)
        );
    }
    
    /**
     * Notification 엔티티를 NotificationResponse DTO로 변환
     */
    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .url(notification.getUrl())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .userId(notification.getUser().getId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
    
    /**
     * 알림 응답 DTO 내부 클래스
     */
    public static class NotificationResponse {
        private final Long id;
        private final NotificationType type;
        private final String title;
        private final String message;
        private final String url;
        private final Boolean isRead;
        private final OffsetDateTime readAt;
        private final Long userId;
        private final OffsetDateTime createdAt;
        
        private NotificationResponse(Long id, NotificationType type, String title, String message,
                                   String url, Boolean isRead, OffsetDateTime readAt, Long userId, OffsetDateTime createdAt) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.message = message;
            this.url = url;
            this.isRead = isRead;
            this.readAt = readAt;
            this.userId = userId;
            this.createdAt = createdAt;
        }
        
        public static NotificationResponseBuilder builder() {
            return new NotificationResponseBuilder();
        }
        
        // Getters
        public Long getId() { return id; }
        public NotificationType getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getUrl() { return url; }
        public Boolean getIsRead() { return isRead; }
        public OffsetDateTime getReadAt() { return readAt; }
        public Long getUserId() { return userId; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        
        public static class NotificationResponseBuilder {
            private Long id;
            private NotificationType type;
            private String title;
            private String message;
            private String url;
            private Boolean isRead;
            private OffsetDateTime readAt;
            private Long userId;
            private OffsetDateTime createdAt;
            
            public NotificationResponseBuilder id(Long id) { this.id = id; return this; }
            public NotificationResponseBuilder type(NotificationType type) { this.type = type; return this; }
            public NotificationResponseBuilder title(String title) { this.title = title; return this; }
            public NotificationResponseBuilder message(String message) { this.message = message; return this; }
            public NotificationResponseBuilder url(String url) { this.url = url; return this; }
            public NotificationResponseBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }
            public NotificationResponseBuilder readAt(OffsetDateTime readAt) { this.readAt = readAt; return this; }
            public NotificationResponseBuilder userId(Long userId) { this.userId = userId; return this; }
            public NotificationResponseBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
            
            public NotificationResponse build() {
                return new NotificationResponse(id, type, title, message, url, isRead, readAt, userId, createdAt);
            }
        }
    }
}








