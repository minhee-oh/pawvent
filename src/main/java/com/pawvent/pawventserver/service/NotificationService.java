package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.Notification;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.NotificationType;
import com.pawvent.pawventserver.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification createNotification(User user, NotificationType type, String title, String message, String url) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .url(url)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public void createLikeNotification(User targetUser, String postTitle, String likerNickname) {
        String title = "게시글에 좋아요가 달렸습니다";
        String message = String.format("%s님이 '%s' 게시글에 좋아요를 눌렀습니다.", likerNickname, postTitle);
        createNotification(targetUser, NotificationType.POST_LIKE, title, message, null);
    }

    @Transactional
    public void createChallengeNotification(User user, String challengeTitle) {
        String title = "챌린지 참여";
        String message = String.format("'%s' 챌린지에 참여하였습니다.", challengeTitle);
        createNotification(user, NotificationType.CHALLENGE, title, message, null);
    }

    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
    }

    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserAndDeletedAtIsNull(user, pageable);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findUnreadNotificationsByUser(user);
    }

    public Page<Notification> getNotificationsByType(User user, NotificationType type, Pageable pageable) {
        List<Notification> list = notificationRepository.findByUserAndType(user, type);
        int start = Math.min((int) pageable.getOffset(), list.size());
        int end = Math.min(start + pageable.getPageSize(), list.size());
        List<Notification> pageContent = list.subList(start, end);
        return new PageImpl<>(pageContent, pageable, list.size());
    }

    @Transactional
    public Notification markAsRead(Long notificationId, User user) {
        Notification notification = getNotificationById(notificationId);
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("알림을 읽음 처리할 권한이 없습니다.");
        }
        Notification updated = notification.toBuilder()
                .isRead(true)
                .readAt(OffsetDateTime.now())
                .build();
        return notificationRepository.save(updated);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadByUser(user);
    }

    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = getNotificationById(notificationId);
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("알림을 삭제할 권한이 없습니다.");
        }
        Notification deleted = notification.toBuilder()
                .deletedAt(OffsetDateTime.now())
                .build();
        notificationRepository.save(deleted);
    }

    @Transactional
    public void deleteAllNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndDeletedAtIsNull(user);
        OffsetDateTime now = OffsetDateTime.now();
        List<Notification> deleted = notifications.stream()
                .map(n -> n.toBuilder().deletedAt(now).build())
                .toList();
        notificationRepository.saveAll(deleted);
    }

    public long getUnreadNotificationCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public long getTotalNotificationCount(User user) {
        return notificationRepository.countByUser(user);
    }
}


