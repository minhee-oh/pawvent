package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.WalkSession;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.Pet;
import com.pawvent.pawventserver.domain.WalkRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 산책 세션 관련 데이터베이스 접근을 담당하는 레포지토리
 * 사용자의 실제 산책 기록을 조회, 저장, 수정하는 기능을 제공
 */
@Repository
public interface WalkSessionRepository extends JpaRepository<WalkSession, Long> {
    
    /**
     * 특정 사용자의 모든 산책 세션을 최신순으로 조회
     * @param user 조회할 사용자
     * @return 해당 사용자의 산책 세션 목록 (최신순)
     */
    @Query("SELECT w FROM WalkSession w WHERE w.user = :user ORDER BY w.startTime DESC")
    List<WalkSession> findByUserOrderByStartTimeDesc(@Param("user") User user);
    
    /**
     * 특정 펫의 모든 산책 세션을 최신순으로 조회
     * @param pet 조회할 펫
     * @return 해당 펫의 산책 세션 목록 (최신순)
     */
    @Query("SELECT w FROM WalkSession w WHERE w.pet = :pet ORDER BY w.startTime DESC")
    List<WalkSession> findByPetOrderByStartTimeDesc(@Param("pet") Pet pet);
    
    /**
     * 완료된 산책 세션만 조회
     * @param user 조회할 사용자
     * @return 완료된 산책 세션 목록
     */
    List<WalkSession> findByUserAndIsCompletedTrue(User user);
    
    /**
     * 진행 중인(미완료) 산책 세션 조회
     * @param user 조회할 사용자
     * @return 진행 중인 산책 세션 (있다면 하나)
     */
    Optional<WalkSession> findByUserAndIsCompletedFalse(User user);
    
    /**
     * 특정 기간 내의 산책 세션 조회
     * @param user 조회할 사용자
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간의 산책 세션 목록
     */
    @Query("SELECT w FROM WalkSession w WHERE w.user = :user " +
           "AND w.startTime >= :startDate AND w.startTime <= :endDate " +
           "ORDER BY w.startTime DESC")
    List<WalkSession> findByUserAndStartTimeBetween(
            @Param("user") User user,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );
    
    /**
     * 특정 루트를 참조한 산책 세션들 조회
     * @param route 조회할 루트
     * @return 해당 루트를 참조한 산책 세션 목록
     */
    List<WalkSession> findByRoute(WalkRoute route);
    
    /**
     * 사용자의 총 산책 거리 계산
     * @param user 조회할 사용자
     * @return 총 산책 거리 (미터 단위)
     */
    @Query("SELECT COALESCE(SUM(w.actualDistance), 0) FROM WalkSession w " +
           "WHERE w.user = :user AND w.isCompleted = true")
    Double getTotalDistanceByUser(@Param("user") User user);
    
    /**
     * 사용자의 총 산책 시간 계산
     * @param user 조회할 사용자
     * @return 총 산책 시간 (초 단위)
     */
    @Query("SELECT COALESCE(SUM(w.actualDuration), 0) FROM WalkSession w " +
           "WHERE w.user = :user AND w.isCompleted = true")
    Long getTotalDurationByUser(@Param("user") User user);
    
    /**
     * 사용자의 총 산책 횟수 조회
     * @param user 조회할 사용자
     * @return 완료된 산책 세션 수
     */
    long countByUserAndIsCompletedTrue(User user);
    
    // 추가 메서드들 (deletedAt 필드 고려)
    List<WalkSession> findByUserAndDeletedAtIsNullOrderByStartTimeDesc(User user);
    
    List<WalkSession> findByPetAndDeletedAtIsNullOrderByStartTimeDesc(Pet pet);
    List<WalkSession> findByRouteAndDeletedAtIsNullOrderByStartTimeDesc(WalkRoute route);
    List<WalkSession> findByUserAndIsCompletedTrueAndDeletedAtIsNullOrderByStartTimeDesc(User user);
    List<WalkSession> findByUserAndIsCompletedFalseAndDeletedAtIsNullOrderByStartTimeDesc(User user);
    List<WalkSession> findByUserAndStartTimeBetweenAndDeletedAtIsNullOrderByStartTimeDesc(
            User user, OffsetDateTime startTime, OffsetDateTime endTime);
    org.springframework.data.domain.Page<WalkSession> findByUserAndDeletedAtIsNullOrderByStartTimeDesc(
            User user, org.springframework.data.domain.Pageable pageable);
    long countByUserAndIsCompletedTrueAndDeletedAtIsNull(User user);
    long countByPetAndIsCompletedTrueAndDeletedAtIsNull(Pet pet);
    
    @Query("SELECT COALESCE(SUM(w.actualDistance), 0) FROM WalkSession w WHERE w.user = :user AND w.isCompleted = true AND w.deletedAt IS NULL")
    double sumDistanceByUserAndIsCompletedTrueAndDeletedAtIsNull(@Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(w.actualDuration), 0) FROM WalkSession w WHERE w.user = :user AND w.isCompleted = true AND w.deletedAt IS NULL")
    int sumDurationByUserAndIsCompletedTrueAndDeletedAtIsNull(@Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(0), 0) FROM WalkSession w WHERE w.user = :user AND w.isCompleted = true AND w.deletedAt IS NULL")
    int sumCaloriesByUserAndIsCompletedTrueAndDeletedAtIsNull(@Param("user") User user);
    
    List<WalkSession> findByUserAndStartTimeBetweenAndIsCompletedTrueAndDeletedAtIsNull(
            User user, OffsetDateTime startTime, OffsetDateTime endTime);
    
    @Query(value = "SELECT r.* FROM walk_route r " +
           "INNER JOIN walk_session w ON w.route_id = r.id " +
           "WHERE w.deleted_at IS NULL AND w.route_id IS NOT NULL " +
           "GROUP BY r.id ORDER BY COUNT(w.id) DESC LIMIT :limit", 
           nativeQuery = true)
    List<WalkRoute> findPopularRoutes(@Param("limit") int limit);
}



