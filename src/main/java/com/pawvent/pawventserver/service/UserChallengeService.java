package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.Challenge;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.UserChallenge;
import com.pawvent.pawventserver.repository.UserChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserChallengeService {
    
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeService challengeService;
    private final NotificationService notificationService;
    
    @Transactional
    public UserChallenge joinChallenge(User user, Challenge challenge) {
        // 이미 참여했는지 확인
        Optional<UserChallenge> existingUserChallenge = 
                userChallengeRepository.findByUserAndChallenge(user, challenge);
        
        if (existingUserChallenge.isPresent()) {
            throw new IllegalArgumentException("이미 참여한 챌린지입니다.");
        }
        
        // 챌린지가 활성화되어 있고 참여 가능한 기간인지 확인
        if (!challengeService.isChallengeActive(challenge.getId())) {
            throw new IllegalArgumentException("참여할 수 없는 챌린지입니다.");
        }
        
        UserChallenge userChallenge = UserChallenge.builder()
                .user(user)
                .challenge(challenge)
                .currentValue(0)
                .isCompleted(false)
                .build();
        
        UserChallenge savedUserChallenge = userChallengeRepository.save(userChallenge);
        
        // 챌린지 참여 알림
        notificationService.createChallengeNotification(user, challenge.getTitle());
        
        return savedUserChallenge;
    }
    
    @Transactional
    public UserChallenge updateProgress(User user, Challenge challenge, int progressValue) {
        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 챌린지입니다."));
        
        int newCurrentValue = userChallenge.getCurrentValue() + progressValue;
        boolean wasCompleted = userChallenge.getIsCompleted();
        boolean isNowCompleted = newCurrentValue >= challenge.getTargetValue();
        
        UserChallenge updatedUserChallenge = userChallenge.toBuilder()
                .currentValue(newCurrentValue)
                .isCompleted(isNowCompleted)
                .completedAt(isNowCompleted && !wasCompleted ? OffsetDateTime.now() : userChallenge.getCompletedAt())
                .build();
        
        UserChallenge saved = userChallengeRepository.save(updatedUserChallenge);
        
        // 챌린지 완료 시 알림
        if (isNowCompleted && !wasCompleted) {
            notificationService.createNotification(
                user,
                com.pawvent.pawventserver.domain.enums.NotificationType.CHALLENGE,
                "챌린지 완료!",
                String.format("'%s' 챌린지를 완료하셨습니다! 축하합니다!", challenge.getTitle()),
                null
            );
        }
        
        return saved;
    }
    
    @Transactional
    public UserChallenge setProgress(User user, Challenge challenge, int currentValue) {
        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 챌린지입니다."));
        
        boolean wasCompleted = userChallenge.getIsCompleted();
        boolean isNowCompleted = currentValue >= challenge.getTargetValue();
        
        UserChallenge updatedUserChallenge = userChallenge.toBuilder()
                .currentValue(currentValue)
                .isCompleted(isNowCompleted)
                .completedAt(isNowCompleted && !wasCompleted ? OffsetDateTime.now() : 
                           (!isNowCompleted ? null : userChallenge.getCompletedAt()))
                .build();
        
        UserChallenge saved = userChallengeRepository.save(updatedUserChallenge);
        
        // 챌린지 완료 시 알림
        if (isNowCompleted && !wasCompleted) {
            notificationService.createNotification(
                user,
                com.pawvent.pawventserver.domain.enums.NotificationType.CHALLENGE,
                "챌린지 완료!",
                String.format("'%s' 챌린지를 완료하셨습니다! 축하합니다!", challenge.getTitle()),
                null
            );
        }
        
        return saved;
    }
    
    @Transactional
    public void leaveChallenge(User user, Challenge challenge) {
        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 챌린지입니다."));
        
        UserChallenge deletedUserChallenge = userChallenge.toBuilder()
                .deletedAt(OffsetDateTime.now())
                .build();
        
        userChallengeRepository.save(deletedUserChallenge);
    }
    
    public UserChallenge getUserChallenge(User user, Challenge challenge) {
        return userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 챌린지입니다."));
    }
    
    public List<UserChallenge> getUserChallenges(User user) {
        return userChallengeRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user);
    }
    
    public Page<UserChallenge> getUserChallenges(User user, Pageable pageable) {
        return userChallengeRepository.findByUserAndDeletedAtIsNull(user, pageable);
    }
    
    public List<UserChallenge> getActiveChallenges(User user) {
        LocalDate now = LocalDate.now();
        return userChallengeRepository.findByUserAndChallenge_IsActiveTrueAndChallenge_StartDateLessThanEqualAndChallenge_EndDateGreaterThanEqualAndDeletedAtIsNull(
                user, now, now);
    }
    
    public List<UserChallenge> getCompletedChallenges(User user) {
        return userChallengeRepository.findByUserAndIsCompletedTrueAndDeletedAtIsNullOrderByCompletedAtDesc(user);
    }
    
    public List<UserChallenge> getIncompleteChallenges(User user) {
        return userChallengeRepository.findByUserAndIsCompletedFalseAndDeletedAtIsNullOrderByCreatedAtDesc(user);
    }
    
    public List<UserChallenge> getChallengeParticipants(Challenge challenge) {
        return userChallengeRepository.findByChallengeAndDeletedAtIsNullOrderByCurrentValueDesc(challenge);
    }
    
    public Page<UserChallenge> getChallengeParticipants(Challenge challenge, Pageable pageable) {
        return userChallengeRepository.findByChallengeAndDeletedAtIsNull(challenge, pageable);
    }
    
    public List<UserChallenge> getChallengeLeaderboard(Challenge challenge, int limit) {
        return userChallengeRepository.findTopByChallengeOrderByCurrentValueDesc(challenge, limit);
    }
    
    public boolean isUserParticipating(User user, Challenge challenge) {
        return userChallengeRepository.findByUserAndChallenge(user, challenge).isPresent();
    }
    
    public long getParticipantCount(Challenge challenge) {
        return userChallengeRepository.countByChallengeAndDeletedAtIsNull(challenge);
    }
    
    public long getCompletionCount(Challenge challenge) {
        return userChallengeRepository.countByChallengeAndIsCompletedTrueAndDeletedAtIsNull(challenge);
    }
    
    public double getCompletionRate(Challenge challenge) {
        long totalParticipants = getParticipantCount(challenge);
        if (totalParticipants == 0) {
            return 0.0;
        }
        
        long completedCount = getCompletionCount(challenge);
        return (double) completedCount / totalParticipants * 100;
    }
    
    public int getUserRanking(User user, Challenge challenge) {
        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 챌린지입니다."));
        
        long betterPerformers = userChallengeRepository.countByChallengeAndCurrentValueGreaterThanAndDeletedAtIsNull(
                challenge, userChallenge.getCurrentValue());
        
        return (int) betterPerformers + 1;
    }
    
    public long getUserCompletedChallengeCount(User user) {
        return userChallengeRepository.countByUserAndIsCompletedTrueAndDeletedAtIsNull(user);
    }
    
    public long getUserTotalChallengeCount(User user) {
        return userChallengeRepository.countByUserAndDeletedAtIsNull(user);
    }
    
    /**
     * 사용자의 평균 챌린지 달성률 계산
     */
    public double getUserAverageCompletionRate(User user) {
        List<UserChallenge> userChallenges = getUserChallenges(user);
        
        if (userChallenges.isEmpty()) {
            return 0.0;
        }
        
        double totalRate = userChallenges.stream()
                .mapToDouble(uc -> {
                    if (uc.getChallenge().getTargetValue() == 0) {
                        return 0.0;
                    }
                    return Math.min(100.0, (double) uc.getCurrentValue() / uc.getChallenge().getTargetValue() * 100);
                })
                .sum();
        
        return totalRate / userChallenges.size();
    }
}








