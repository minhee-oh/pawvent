package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 챌린지 관련 데이터베이스 접근을 담당하는 레포지토리
 * 챌린지 조회, 저장, 수정 기능을 제공
 */
@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    
    /**
     * 모든 챌린지를 생성일 최신순으로 조회
     * @return 챌린지 목록 (최신순)
     */
    @Query("SELECT c FROM Challenge c ORDER BY c.createdAt DESC")
    List<Challenge> findAllOrderByCreatedAtDesc();
    
    /**
     * 현재 진행 중인 챌린지들 조회
     * @param currentDate 현재 날짜
     * @return 진행 중인 챌린지 목록
     */
    @Query("SELECT c FROM Challenge c WHERE c.startDate <= :currentDate AND c.endDate >= :currentDate")
    List<Challenge> findActiveChallenges(@Param("currentDate") LocalDate currentDate);
    
    /**
     * 향후 시작될 챌린지들 조회
     * @param currentDate 현재 날짜
     * @return 예정된 챌린지 목록
     */
    @Query("SELECT c FROM Challenge c WHERE c.startDate > :currentDate ORDER BY c.startDate ASC")
    List<Challenge> findUpcomingChallenges(@Param("currentDate") LocalDate currentDate);
    
    /**
     * 종료된 챌린지들 조회
     * @param currentDate 현재 날짜
     * @return 종료된 챌린지 목록
     */
    @Query("SELECT c FROM Challenge c WHERE c.endDate < :currentDate ORDER BY c.endDate DESC")
    List<Challenge> findExpiredChallenges(@Param("currentDate") LocalDate currentDate);
    
    /**
     * 특정 기간 내에 시작하는 챌린지들 조회
     * @param startDate 조회 시작 날짜
     * @param endDate 조회 종료 날짜
     * @return 해당 기간의 챌린지 목록
     */
    @Query("SELECT c FROM Challenge c WHERE c.startDate BETWEEN :startDate AND :endDate ORDER BY c.startDate ASC")
    List<Challenge> findChallengesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 활성화된 챌린지들을 시작일 내림차순으로 조회
     * @return 활성화된 챌린지 목록
     */
    List<Challenge> findByIsActiveTrueOrderByStartDateDesc();
    
    /**
     * 특정 날짜 범위에서 활성화된 챌린지들 조회
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간의 활성화된 챌린지 목록
     */
    @Query("SELECT c FROM Challenge c WHERE c.isActive = true AND c.startDate <= :endDate AND c.endDate >= :startDate ORDER BY c.startDate ASC")
    List<Challenge> findActiveChallengesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    @Query("SELECT c FROM Challenge c WHERE c.startDate >= :startDate AND c.startDate <= :endDate ORDER BY c.startDate ASC")
    List<Challenge> findChallengesStartingBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 챌린지 제목으로 검색 (부분 일치, 대소문자 무시)
     * @param title 검색할 제목
     * @return 검색된 챌린지 목록
     */
    @Query("SELECT c FROM Challenge c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY c.createdAt DESC")
    List<Challenge> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    /**
     * 챌린지 설명으로 검색 (부분 일치, 대소문자 무시)
     * @param description 검색할 설명
     * @return 검색된 챌린지 목록
     */
    @Query("SELECT c FROM Challenge c WHERE LOWER(c.description) LIKE LOWER(CONCAT('%', :description, '%')) ORDER BY c.createdAt DESC")
    List<Challenge> findByDescriptionContainingIgnoreCase(@Param("description") String description);
    
    /**
     * 챌린지 제목 또는 설명으로 검색
     * @param keyword 검색 키워드
     * @return 검색된 챌린지 목록
     */
    @Query("SELECT c FROM Challenge c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY c.createdAt DESC")
    List<Challenge> findByTitleOrDescriptionContaining(@Param("keyword") String keyword);
    
    /**
     * 특정 날짜에 진행 중인 챌린지 수 조회
     * @param date 확인할 날짜
     * @return 해당 날짜에 진행 중인 챌린지 수
     */
    @Query("SELECT COUNT(c) FROM Challenge c WHERE c.startDate <= :date AND c.endDate >= :date")
    long countActiveChallengesOnDate(@Param("date") LocalDate date);
}
