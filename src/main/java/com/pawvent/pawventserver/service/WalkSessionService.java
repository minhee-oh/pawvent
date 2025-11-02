package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.Pet;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.WalkRoute;
import com.pawvent.pawventserver.domain.WalkSession;
import com.pawvent.pawventserver.repository.WalkSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkSessionService {
    
    private final WalkSessionRepository walkSessionRepository;
    
    /**
     * 새로운 산책 세션을 시작합니다.
     * 사용자가 특정 반려동물과 함께 선택한 경로로 산책을 시작할 때 호출됩니다.
     * 시작 시간이 자동으로 기록되며, 완료 상태는 false로 설정됩니다.
     * 
     * @param user 산책을 하는 사용자
     * @param pet 산책에 참여하는 반려동물
     * @param route 산책할 경로
     * @return 생성된 산책 세션 엔티티
     */
    @Transactional
    public WalkSession startWalkSession(User user, Pet pet, WalkRoute route) {
        WalkSession walkSession = WalkSession.builder()
                .user(user)
                .pet(pet)
                .route(route)
                .startTime(OffsetDateTime.now())
                .isCompleted(false)
                .build();
        
        return walkSessionRepository.save(walkSession);
    }
    
    /**
     * 진행 중인 산책 세션을 완료합니다.
     * 산책 결과데이터(거리, 소요시간, 칼로리)와 사진, 메모를 추가할 수 있습니다.
     * 세션 주인만 완료할 수 있으며, 이미 완료된 세션은 다시 완료할 수 없습니다.
     * 
     * @param sessionId 완료할 세션의 ID
     * @param user 산책 세션 주인 (권한 검증용)
     * @param distance 실제 산책 거리 (미터 단위)
     * @param duration 실제 산책 시간 (초 단위)
     * @param calories 소모 칼로리
     * @param imageUrls 산책 중 촬영한 사진들의 URL 목록
     * @param memo 산책 후기 또는 메모
     * @return 완료된 산책 세션
     * @throws IllegalArgumentException 권한이 없거나 이미 완료된 세션인 경우
     */
    @Transactional
    public WalkSession completeWalkSession(Long sessionId, User user, double distance, int duration) {
        WalkSession walkSession = getWalkSessionById(sessionId);
        
        if (!walkSession.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("산책 세션을 완료할 권한이 없습니다.");
        }
        
        if (walkSession.getIsCompleted()) {
            throw new IllegalArgumentException("이미 완료된 산책 세션입니다.");
        }
        
        WalkSession completedSession = walkSession.toBuilder()
                .endTime(OffsetDateTime.now())
                .actualDistance(distance)
                .actualDuration(duration)
                .isCompleted(true)
                .build();
        
        return walkSessionRepository.save(completedSession);
    }
    
    /**
     * 완료된 산책 세션의 정보를 수정합니다.
     * 거리, 시간, 칼로리, 사진, 메모 등을 업데이트할 수 있습니다.
     * 세션 주인만 수정할 수 있습니다.
     * 
     * @param sessionId 수정할 세션 ID
     * @param user 세션 주인 (권한 검증용)
     * @param distance 수정할 거리 정보
     * @param duration 수정할 소요시간
     * @param calories 수정할 칼로리 정보
     * @param imageUrls 수정할 사진 URL 목록
     * @param memo 수정할 메모
     * @return 수정된 산책 세션
     * @throws IllegalArgumentException 수정 권한이 없는 경우
     */
    @Transactional
    public WalkSession updateWalkSession(Long sessionId, User user, double distance, int duration) {
        WalkSession walkSession = getWalkSessionById(sessionId);
        
        if (!walkSession.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("산책 세션을 수정할 권한이 없습니다.");
        }
        
        WalkSession updatedSession = walkSession.toBuilder()
                .actualDistance(distance)
                .actualDuration(duration)
                .build();
        
        return walkSessionRepository.save(updatedSession);
    }
    
    /**
     * 진행 중인 산책 세션을 취소합니다 (소프트 삭제).
     * 실수로 시작한 세션이나 중단하고 싶은 세션을 취소할 때 사용합니다.
     * 세션 주인만 취소할 수 있습니다.
     * 
     * @param sessionId 취소할 세션 ID
     * @param user 세션 주인 (권한 검증용)
     * @throws IllegalArgumentException 취소 권한이 없는 경우
     */
    @Transactional
    public void cancelWalkSession(Long sessionId, User user) {
        WalkSession walkSession = getWalkSessionById(sessionId);
        
        if (!walkSession.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("산책 세션을 취소할 권한이 없습니다.");
        }
        
        WalkSession cancelledSession = walkSession.toBuilder()
                .deletedAt(OffsetDateTime.now())
                .build();
        
        walkSessionRepository.save(cancelledSession);
    }
    
    /**
     * ID로 특정 산책 세션을 조회합니다.
     * 
     * @param sessionId 조회할 세션의 고유 ID
     * @return 해당 산책 세션 엔티티
     * @throws IllegalArgumentException 세션을 찾을 수 없는 경우
     */
    public WalkSession getWalkSessionById(Long sessionId) {
        return walkSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("산책 세션을 찾을 수 없습니다."));
    }
    
    /**
     * 특정 사용자의 모든 산책 세션을 조회합니다.
     * 삭제되지 않은 세션만 최신순으로 반환합니다.
     * 
     * @param user 산책 세션을 조회할 사용자
     * @return 해당 사용자의 산책 세션 목록 (최신순)
     */
    public List<WalkSession> getWalkSessionsByUser(User user) {
        return walkSessionRepository.findByUserAndDeletedAtIsNullOrderByStartTimeDesc(user);
    }
    
    public Page<WalkSession> getWalkSessionsByUser(User user, Pageable pageable) {
        return walkSessionRepository.findByUserAndDeletedAtIsNullOrderByStartTimeDesc(user, pageable);
    }
    
    public List<WalkSession> getWalkSessionsByPet(Pet pet) {
        return walkSessionRepository.findByPetAndDeletedAtIsNullOrderByStartTimeDesc(pet);
    }
    
    public List<WalkSession> getWalkSessionsByRoute(WalkRoute route) {
        return walkSessionRepository.findByRouteAndDeletedAtIsNullOrderByStartTimeDesc(route);
    }
    
    public List<WalkSession> getCompletedWalkSessions(User user) {
        return walkSessionRepository.findByUserAndIsCompletedTrueAndDeletedAtIsNullOrderByStartTimeDesc(user);
    }
    
    public List<WalkSession> getActiveWalkSessions(User user) {
        return walkSessionRepository.findByUserAndIsCompletedFalseAndDeletedAtIsNullOrderByStartTimeDesc(user);
    }
    
    public List<WalkSession> getWalkSessionsByDate(User user, LocalDate date) {
        OffsetDateTime startOfDay = date.atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime();
        
        return walkSessionRepository.findByUserAndStartTimeBetweenAndDeletedAtIsNullOrderByStartTimeDesc(
                user, startOfDay, endOfDay);
    }
    
    public List<WalkSession> getWalkSessionsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        OffsetDateTime startDateTime = startDate.atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime endDateTime = endDate.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime();
        
        return walkSessionRepository.findByUserAndStartTimeBetweenAndDeletedAtIsNullOrderByStartTimeDesc(
                user, startDateTime, endDateTime);
    }
    
    public List<WalkSession> getRecentWalkSessions(User user, int limit) {
        org.springframework.data.domain.Page<WalkSession> page =
                walkSessionRepository.findByUserAndDeletedAtIsNullOrderByStartTimeDesc(
                        user,
                        org.springframework.data.domain.PageRequest.of(0, Math.max(1, limit))
                );
        return page.getContent();
    }
    
    public boolean isWalkSessionOwner(Long sessionId, User user) {
        WalkSession walkSession = getWalkSessionById(sessionId);
        return walkSession.getUser().getId().equals(user.getId());
    }
    
    public long getUserWalkSessionCount(User user) {
        return walkSessionRepository.countByUserAndIsCompletedTrueAndDeletedAtIsNull(user);
    }
    
    public long getPetWalkSessionCount(Pet pet) {
        return walkSessionRepository.countByPetAndIsCompletedTrueAndDeletedAtIsNull(pet);
    }
    
    public double getUserTotalDistance(User user) {
        return walkSessionRepository.sumDistanceByUserAndIsCompletedTrueAndDeletedAtIsNull(user);
    }
    
    public int getUserTotalDuration(User user) {
        return walkSessionRepository.sumDurationByUserAndIsCompletedTrueAndDeletedAtIsNull(user);
    }
    
    public int getUserTotalCalories(User user) {
        return walkSessionRepository.sumCaloriesByUserAndIsCompletedTrueAndDeletedAtIsNull(user);
    }
    
    /**
     * 특정 기간 동안의 사용자 통계 조회
     */
    public WalkStats getUserWalkStats(User user, LocalDate startDate, LocalDate endDate) {
        OffsetDateTime startDateTime = startDate.atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime endDateTime = endDate.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime();
        
        List<WalkSession> sessions = walkSessionRepository
                .findByUserAndStartTimeBetweenAndIsCompletedTrueAndDeletedAtIsNull(
                        user, startDateTime, endDateTime);
        
        long totalSessions = sessions.size();
        double totalDistance = sessions.stream()
                .filter(s -> s.getActualDistance() != null)
                .mapToDouble(WalkSession::getActualDistance).sum();
        int totalDuration = sessions.stream()
                .filter(s -> s.getActualDuration() != null)
                .mapToInt(WalkSession::getActualDuration).sum();
        
        double averageDistance = totalSessions > 0 ? totalDistance / totalSessions : 0;
        double averageDuration = totalSessions > 0 ? (double) totalDuration / totalSessions : 0;
        
        return new WalkStats(totalSessions, totalDistance, totalDuration, 0, 
                           averageDistance, averageDuration);
    }
    
    /**
     * 월별 산책 통계 조회
     */
    public WalkStats getUserMonthlyStats(User user, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        return getUserWalkStats(user, startDate, endDate);
    }
    
    /**
     * 주별 산책 통계 조회
     */
    public WalkStats getUserWeeklyStats(User user, LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return getUserWalkStats(user, startOfWeek, endOfWeek);
    }
    
    /**
     * 가장 인기 있는 산책 경로 조회
     */
    public List<WalkRoute> getPopularWalkRoutes(int limit) {
        return walkSessionRepository.findPopularRoutes(limit);
    }
    
    /**
     * 산책 통계를 담는 클래스
     */
    public static class WalkStats {
        private final long totalSessions;
        private final double totalDistance;
        private final int totalDuration;
        private final int totalCalories;
        private final double averageDistance;
        private final double averageDuration;
        
        public WalkStats(long totalSessions, double totalDistance, int totalDuration, 
                        int totalCalories, double averageDistance, double averageDuration) {
            this.totalSessions = totalSessions;
            this.totalDistance = totalDistance;
            this.totalDuration = totalDuration;
            this.totalCalories = totalCalories;
            this.averageDistance = averageDistance;
            this.averageDuration = averageDuration;
        }
        
        // Getters
        public long getTotalSessions() { return totalSessions; }
        public double getTotalDistance() { return totalDistance; }
        public int getTotalDuration() { return totalDuration; }
        public int getTotalCalories() { return totalCalories; }
        public double getAverageDistance() { return averageDistance; }
        public double getAverageDuration() { return averageDuration; }
    }
}
