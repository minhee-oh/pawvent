package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.Challenge;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.UserChallenge;
import com.pawvent.pawventserver.repository.ChallengeRepository;
import com.pawvent.pawventserver.repository.UserChallengeRepository;
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
public class ChallengeService {
    
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    
    /**
     * 새로운 챌린지를 생성합니다.
     * 챌린지는 산책 거리, 시간, 횟수 등의 목표를 가질 수 있습니다.
     * 기본적으로 활성 상태로 생성되며, 많은 사용자가 참여할 수 있습니다.
     * 
     * @param title 챌린지 제목
     * @param description 챌린지 설명
     * @param targetValue 목표 값 (거리, 횟수 등)
     * @param startDate 챌린지 시작 날짜
     * @param endDate 챌린지 종료 날짜
     * @param imageUrl 챌린지 이미지 URL
     * @return 생성된 챌린지 엔티티
     */
    @Transactional
    public Challenge createChallenge(String title, String description, int targetValue, 
                                   LocalDate startDate, LocalDate endDate, String imageUrl) {
        Challenge challenge = Challenge.builder()
                .title(title)
                .description(description)
                .targetValue(targetValue)
                .startDate(startDate)
                .endDate(endDate)
                .imageUrl(imageUrl)
                .isActive(true)
                .build();
        
        return challengeRepository.save(challenge);
    }
    
    /**
     * ID로 특정 챌린지를 조회합니다.
     * 
     * @param challengeId 조회할 챌린지의 고유 ID
     * @return 챌린지 엔티티
     * @throws IllegalArgumentException 챌린지를 찾을 수 없는 경우
     */
    public Challenge getChallengeById(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다."));
    }
    
    /**
     * 활성화된 모든 챌린지를 시작일 내림차순으로 조회합니다.
     * 사용자가 참여할 수 있는 챌린지 목록을 보여줍니다.
     * 
     * @return 활성 챌린지 목록 (최신순)
     */
    public List<Challenge> getActiveChallenges() {
        return challengeRepository.findByIsActiveTrueOrderByStartDateDesc();
    }
    
    /**
     * 모든 챌린지를 페이지 단위로 조회합니다.
     * 관리자가 전체 챌린지 목록을 확인할 때 사용합니다.
     * 
     * @param pageable 페이지 정보 (페이지 번호, 사이즈, 정렬)
     * @return 페이지네이션된 챌린지 목록
     */
    public Page<Challenge> getAllChallenges(Pageable pageable) {
        return challengeRepository.findAll(pageable);
    }
    
    /**
     * 현재 진행 중인 챌린지들을 조회합니다.
     * 오늘 날짜가 시작일과 종료일 사이에 있는 챌린지들을 반환합니다.
     * 
     * @return 진행 중인 챌린지 목록
     */
    public List<Challenge> getCurrentChallenges() {
        LocalDate now = LocalDate.now();
        return challengeRepository.findActiveChallengesByDateRange(now, now);
    }
    
    /**
     * 기존 챌린지의 정보를 업데이트합니다.
     * 제목, 설명, 목표값, 기간, 이미지 등을 수정할 수 있습니다.
     * 존재하는 챌린지만 수정 가능합니다.
     * 
     * @param challengeId 수정할 챌린지 ID
     * @param title 새 제목
     * @param description 새 설명
     * @param targetValue 새 목표값
     * @param startDate 새 시작 날짜
     * @param endDate 새 종료 날짜
     * @param imageUrl 새 이미지 URL
     * @return 업데이트된 챌린지 엔티티
     * @throws IllegalArgumentException 챌린지를 찾을 수 없는 경우
     */
    @Transactional
    public Challenge updateChallenge(Long challengeId, String title, String description, 
                                   int targetValue, LocalDate startDate, LocalDate endDate, String imageUrl) {
        Challenge challenge = getChallengeById(challengeId);
        
        challenge = challenge.toBuilder()
                .title(title)
                .description(description)
                .targetValue(targetValue)
                .startDate(startDate)
                .endDate(endDate)
                .imageUrl(imageUrl)
                .build();
        
        return challengeRepository.save(challenge);
    }
    
    /**
     * 챌린지를 비활성화합니다.
     * 물리적으로 삭제하지 않고 isActive 플래그를 false로 설정합니다.
     * 더 이상 새로운 사용자 참여를 받지 않습니다.
     * 
     * @param challengeId 비활성화할 챌린지 ID
     * @throws IllegalArgumentException 챌린지를 찾을 수 없는 경우
     */
    @Transactional
    public void deactivateChallenge(Long challengeId) {
        Challenge challenge = getChallengeById(challengeId);
        
        Challenge updatedChallenge = challenge.toBuilder()
                .isActive(false)
                .build();
        
        challengeRepository.save(updatedChallenge);
    }
    
    /**
     * 챌린지를 완전히 삭제합니다 (소프트 삭제).
     * deletedAt 필드를 설정하여 논리적으로 삭제합니다.
     * 이미 참여한 사용자들의 데이터는 보존됩니다.
     * 
     * @param challengeId 삭제할 챌린지 ID
     * @throws IllegalArgumentException 챌린지를 찾을 수 없는 경우
     */
    @Transactional
    public void deleteChallenge(Long challengeId) {
        Challenge challenge = getChallengeById(challengeId);
        
        Challenge deletedChallenge = challenge.toBuilder()
                .deletedAt(OffsetDateTime.now())
                .isActive(false)
                .build();
        
        challengeRepository.save(deletedChallenge);
    }
    
    public boolean isChallengeActive(Long challengeId) {
        Challenge challenge = getChallengeById(challengeId);
        LocalDate now = LocalDate.now();
        return challenge.getIsActive() && 
               !now.isBefore(challenge.getStartDate()) && 
               !now.isAfter(challenge.getEndDate());
    }
    
    public List<Challenge> getUserParticipatedChallenges(User user) {
        return userChallengeRepository.findByUser(user)
                .stream()
                .map(UserChallenge::getChallenge)
                .toList();
    }
    
    public long getParticipantCount(Long challengeId) {
        return userChallengeRepository.countByChallenge_Id(challengeId);
    }
}
