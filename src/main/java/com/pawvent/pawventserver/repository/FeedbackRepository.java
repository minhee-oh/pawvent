package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.Feedback;
import com.pawvent.pawventserver.domain.Hazard;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 피드백/정정 요청 관련 데이터베이스 접근을 담당하는 레포지토리
 * 위험요소에 대한 사용자 피드백 및 정정 요청을 관리하는 기능을 제공
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    /**
     * 특정 사용자의 모든 피드백을 최신순으로 조회
     * @param user 사용자
     * @return 해당 사용자의 피드백 목록 (최신순)
     */
    @Query("SELECT f FROM Feedback f WHERE f.user = :user ORDER BY f.createdAt DESC")
    List<Feedback> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    /**
     * 특정 위험요소의 모든 피드백을 최신순으로 조회
     * @param hazard 위험요소
     * @return 해당 위험요소의 피드백 목록 (최신순)
     */
    @Query("SELECT f FROM Feedback f WHERE f.hazard = :hazard ORDER BY f.createdAt DESC")
    List<Feedback> findByHazardOrderByCreatedAtDesc(@Param("hazard") Hazard hazard);
    
    /**
     * 특정 상태의 모든 피드백을 최신순으로 조회
     * @param status 피드백 상태
     * @return 해당 상태의 피드백 목록 (최신순)
     */
    @Query("SELECT f FROM Feedback f WHERE f.status = :status ORDER BY f.createdAt DESC")
    List<Feedback> findByStatusOrderByCreatedAtDesc(@Param("status") FeedbackStatus status);
    
    /**
     * 특정 상태의 피드백을 페이징으로 조회 (관리자용)
     * @param status 피드백 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 피드백 페이지
     */
    @Query("SELECT f FROM Feedback f WHERE f.status = :status ORDER BY f.createdAt DESC")
    Page<Feedback> findByStatusOrderByCreatedAtDesc(@Param("status") FeedbackStatus status, Pageable pageable);
    
    /**
     * 특정 사용자의 특정 상태 피드백들 조회
     * @param user 사용자
     * @param status 피드백 상태
     * @return 해당 조건의 피드백 목록
     */
    @Query("SELECT f FROM Feedback f WHERE f.user = :user AND f.status = :status ORDER BY f.createdAt DESC")
    List<Feedback> findByUserAndStatus(@Param("user") User user, @Param("status") FeedbackStatus status);
    
    /**
     * 특정 위험요소의 특정 상태 피드백들 조회
     * @param hazard 위험요소
     * @param status 피드백 상태
     * @return 해당 조건의 피드백 목록
     */
    @Query("SELECT f FROM Feedback f WHERE f.hazard = :hazard AND f.status = :status ORDER BY f.createdAt DESC")
    List<Feedback> findByHazardAndStatus(@Param("hazard") Hazard hazard, @Param("status") FeedbackStatus status);
    
    /**
     * 특정 사용자의 피드백 수 조회
     * @param user 사용자
     * @return 해당 사용자의 총 피드백 수
     */
    long countByUser(User user);
    
    /**
     * 특정 위험요소의 피드백 수 조회
     * @param hazard 위험요소
     * @return 해당 위험요소의 총 피드백 수
     */
    long countByHazard(Hazard hazard);
    
    /**
     * 특정 상태의 피드백 수 조회
     * @param status 피드백 상태
     * @return 해당 상태의 총 피드백 수
     */
    long countByStatus(FeedbackStatus status);
    
    /**
     * 특정 사용자의 특정 상태 피드백 수 조회
     * @param user 사용자
     * @param status 피드백 상태
     * @return 해당 조건의 피드백 수
     */
    long countByUserAndStatus(User user, FeedbackStatus status);
    
    /**
     * 특정 위험요소의 특정 상태 피드백 수 조회
     * @param hazard 위험요소
     * @param status 피드백 상태
     * @return 해당 조건의 피드백 수
     */
    long countByHazardAndStatus(Hazard hazard, FeedbackStatus status);
    
    /**
     * 가장 많은 피드백을 받은 위험요소들 조회
     * @return 피드백 수가 많은 위험요소 목록
     */
    @Query("SELECT f.hazard, COUNT(f) as feedbackCount FROM Feedback f " +
           "GROUP BY f.hazard ORDER BY COUNT(f) DESC")
    List<Object[]> findMostFeedbackReceivingHazards();
    
    // 추가 필요한 메서드들 - FeedbackService에서 사용
    Page<Feedback> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
    List<Feedback> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(User user);
    Page<Feedback> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(FeedbackStatus status, Pageable pageable);
    List<Feedback> findByStatusAndDeletedAtIsNullOrderByCreatedAtAsc(FeedbackStatus status);
    long countByStatusAndDeletedAtIsNull(FeedbackStatus status);
    long countByUserAndDeletedAtIsNull(User user);
    long countByDeletedAtIsNull();
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.status IN :statuses AND f.deletedAt IS NULL")
    long countByStatusInAndDeletedAtIsNull(@Param("statuses") List<FeedbackStatus> statuses);
    
    @Query("SELECT f.hazard, COUNT(f) as feedbackCount FROM Feedback f WHERE f.deletedAt IS NULL " +
           "GROUP BY f.hazard ORDER BY COUNT(f) DESC")
    List<Object[]> findHazardsWithMostFeedback();
    
    /**
     * 미처리 피드백들을 오래된 순으로 조회
     * @return 처리 대기 중인 피드백 목록 (오래된 순)
     */
    @Query("SELECT f FROM Feedback f WHERE f.status = :status AND f.deletedAt IS NULL ORDER BY f.createdAt ASC")
    List<Feedback> findPendingFeedbacksOrderByCreatedAtAsc(@Param("status") FeedbackStatus status);
}

