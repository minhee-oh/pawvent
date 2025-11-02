package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.Notification;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 알림 관련 데이터베이스 접근을 담당하는 레포지토리
 * 사용자 알림의 조회, 저장, 수정, 삭제 기능을 제공
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 특정 사용자의 모든 알림을 최신순으로 조회
     * @param user 사용자
     * @return 해당 사용자의 알림 목록 (최신순)
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    List<Notification> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    /**
     * 특정 사용자의 알림을 페이징으로 조회
     * @param user 사용자
     * @param pageable 페이징 정보
     * @return 해당 사용자의 알림 페이지
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    Page<Notification> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);
    
    /**
     * 특정 사용자의 읽지 않은 알림들 조회
     * @param user 사용자
     * @return 해당 사용자의 읽지 않은 알림 목록
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByUser(@Param("user") User user);
    
    /**
     * 특정 사용자의 읽은 알림들 조회
     * @param user 사용자
     * @return 해당 사용자의 읽은 알림 목록
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = true ORDER BY n.createdAt DESC")
    List<Notification> findReadNotificationsByUser(@Param("user") User user);
    
    /**
     * 특정 사용자의 특정 타입 알림들 조회
     * @param user 사용자
     * @param type 알림 타입
     * @return 해당 조건의 알림 목록
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.type = :type ORDER BY n.createdAt DESC")
    List<Notification> findByUserAndType(@Param("user") User user, @Param("type") NotificationType type);
    
    /**
     * 특정 사용자의 읽지 않은 알림 수 조회
     * @param user 사용자
     * @return 해당 사용자의 읽지 않은 알림 수
     */
    long countByUserAndIsReadFalse(User user);
    
    /**
     * 특정 사용자의 총 알림 수 조회
     * @param user 사용자
     * @return 해당 사용자의 총 알림 수
     */
    long countByUser(User user);
    
    /**
     * 특정 사용자의 특정 타입 읽지 않은 알림 수 조회
     * @param user 사용자
     * @param type 알림 타입
     * @return 해당 조건의 알림 수
     */
    long countByUserAndTypeAndIsReadFalse(User user, NotificationType type);
    
    /**
     * 특정 사용자의 모든 알림을 읽음으로 표시
     * @param user 사용자
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadByUser(@Param("user") User user);
    
    /**
     * 특정 사용자의 특정 타입 알림들을 읽음으로 표시
     * @param user 사용자
     * @param type 알림 타입
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.type = :type AND n.isRead = false")
    void markAsReadByUserAndType(@Param("user") User user, @Param("type") NotificationType type);
    
    /**
     * 특정 사용자의 읽은 알림들 삭제
     * @param user 사용자
     */
    void deleteByUserAndIsReadTrue(User user);
    
    /**
     * 특정 사용자의 모든 알림 삭제
     * @param user 사용자
     */
    void deleteByUser(User user);
    
    /**
     * 삭제되지 않은 특정 사용자의 알림 조회
     * @param user 사용자
     * @return 삭제되지 않은 알림 목록
     */
    List<Notification> findByUserAndDeletedAtIsNull(User user);
    
    /**
     * 삭제되지 않은 특정 사용자의 알림 페이징 조회
     * @param user 사용자
     * @param pageable 페이징 정보
     * @return 삭제되지 않은 알림 페이지
     */
    Page<Notification> findByUserAndDeletedAtIsNull(User user, Pageable pageable);
}



