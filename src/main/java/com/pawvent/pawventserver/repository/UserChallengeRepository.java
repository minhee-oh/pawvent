package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.UserChallenge;
import com.pawvent.pawventserver.domain.Challenge;
import com.pawvent.pawventserver.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 챌린지 참여 관련 데이터베이스 접근을 담당하는 레포지토리
 * 사용자의 챌린지 참여, 진행도, 성공 여부를 관리하는 기능을 제공
 */
@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    
    /**
     * 특정 사용자의 모든 챌린지 참여 기록을 최신순으로 조회
     * @param user 사용자
     * @return 해당 사용자의 챌린지 참여 목록
     */
    @Query("SELECT uc FROM UserChallenge uc WHERE uc.user = :user ORDER BY uc.createdAt DESC")
    List<UserChallenge> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    /**
     * 특정 챌린지의 모든 참여자 조회
     * @param challenge 챌린지
     * @return 해당 챌린지의 참여자 목록
     */
    List<UserChallenge> findByChallenge(Challenge challenge);
    
    /**
     * 특정 사용자가 특정 챌린지에 참여했는지 확인
     * @param user 사용자
     * @param challenge 챌린지
     * @return 참여 기록 (없으면 Empty)
     */
    Optional<UserChallenge> findByUserAndChallenge(User user, Challenge challenge);
    
    /**
     * 특정 사용자가 특정 챌린지에 참여했는지 여부 확인
     * @param user 사용자
     * @param challenge 챌린지
     * @return 참여 여부
     */
    boolean existsByUserAndChallenge(User user, Challenge challenge);
    
    // 새로운 필드명에 맞춘 메서드들
    List<UserChallenge> findByUser(User user);
    
    List<UserChallenge> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(User user);
    
    Page<UserChallenge> findByUserAndDeletedAtIsNull(User user, Pageable pageable);
    
    List<UserChallenge> findByUserAndIsCompletedTrueAndDeletedAtIsNullOrderByCompletedAtDesc(User user);
    
    List<UserChallenge> findByUserAndIsCompletedFalseAndDeletedAtIsNullOrderByCreatedAtDesc(User user);
    
    List<UserChallenge> findByChallengeAndDeletedAtIsNullOrderByCurrentValueDesc(Challenge challenge);
    
    Page<UserChallenge> findByChallengeAndDeletedAtIsNull(Challenge challenge, Pageable pageable);
    
    @Query("SELECT uc FROM UserChallenge uc WHERE uc.challenge = :challenge AND uc.deletedAt IS NULL ORDER BY uc.currentValue DESC")
    List<UserChallenge> findTopByChallengeOrderByCurrentValueDesc(@Param("challenge") Challenge challenge, @Param("limit") int limit);
    
    long countByChallenge_Id(Long challengeId);
    
    long countByChallengeAndDeletedAtIsNull(Challenge challenge);
    
    long countByChallengeAndIsCompletedTrueAndDeletedAtIsNull(Challenge challenge);
    
    long countByUserAndIsCompletedTrueAndDeletedAtIsNull(User user);
    
    long countByUserAndDeletedAtIsNull(User user);
    
    long countByChallengeAndCurrentValueGreaterThanAndDeletedAtIsNull(Challenge challenge, Integer currentValue);
    
    List<UserChallenge> findByUserAndChallenge_IsActiveTrueAndChallenge_StartDateLessThanEqualAndChallenge_EndDateGreaterThanEqualAndDeletedAtIsNull(
        User user, LocalDate startDate, LocalDate endDate);
}



