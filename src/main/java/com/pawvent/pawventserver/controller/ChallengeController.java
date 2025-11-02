package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.domain.Challenge;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.UserChallenge;
import com.pawvent.pawventserver.service.ChallengeService;
import com.pawvent.pawventserver.service.UserChallengeService;
import com.pawvent.pawventserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 챌린지 관리 컨트롤러
 * 
 * 챌린지 조회, 참여, 진행률 관리 등의 기능을 제공합니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {
    
    private final ChallengeService challengeService;
    private final UserChallengeService userChallengeService;
    private final UserService userService;
    
    /**
     * 모든 활성 챌린지 목록을 조회합니다.
     * 사용자가 참여할 수 있는 챌린지들을 최신순으로 보여줍니다.
     * 
     * @return 활성 챌린지 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChallengeResponse>>> getActiveChallenges() {
        List<Challenge> challenges = challengeService.getActiveChallenges();
        List<ChallengeResponse> challengeResponses = challenges.stream()
                .map(this::mapToChallengeResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("활성 챌린지를 조회했습니다.", challengeResponses)
        );
    }
    
    /**
     * 모든 챌린지를 페이지 단위로 조회합니다.
     * 관리자가 전체 챌린지 목록을 확인할 때 사용합니다.
     * 
     * @param pageable 페이지 정보 (페이지 번호, 사이즈, 정렬)
     * @return 페이지너이션된 챌린지 목록
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<ChallengeResponse>>> getAllChallenges(Pageable pageable) {
        Page<Challenge> challenges = challengeService.getAllChallenges(pageable);
        Page<ChallengeResponse> challengeResponses = challenges.map(this::mapToChallengeResponse);
        
        return ResponseEntity.ok(
            ApiResponse.success("모든 챌린지를 조회했습니다.", challengeResponses)
        );
    }
    
    /**
     * 오늘 날짜에 진행 중인 챌린지들을 조회합니다.
     * 시작일과 종료일 사이에 현재 날짜가 있는 챌린지만 반환합니다.
     * 메인 화면에서 노출되는 챌린지 목록입니다.
     * 
     * @return 진행 중인 챌린지 목록
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<List<ChallengeResponse>>> getCurrentChallenges() {
        List<Challenge> challenges = challengeService.getCurrentChallenges();
        List<ChallengeResponse> challengeResponses = challenges.stream()
                .map(this::mapToChallengeResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("현재 진행 중인 챌린지를 조회했습니다.", challengeResponses)
        );
    }
    
    /**
     * ID로 특정 챌린지의 상세 정보를 조회합니다.
     * 챌린지의 제목, 설명, 기간, 목표, 참여 조건 등을 확인할 수 있습니다.
     * 
     * @param challengeId 조회할 챌린지의 ID
     * @return 챌린지 상세 정보
     */
    @GetMapping("/{challengeId}")
    public ResponseEntity<ApiResponse<ChallengeResponse>> getChallenge(@PathVariable Long challengeId) {
        Challenge challenge = challengeService.getChallengeById(challengeId);
        ChallengeResponse challengeResponse = mapToChallengeResponse(challenge);
        
        return ResponseEntity.ok(
            ApiResponse.success("챌린지를 조회했습니다.", challengeResponse)
        );
    }
    
    /**
     * 사용자가 특정 챌린지에 참여합니다.
     * 참여 조건을 확인하고 참여가 가능한 경우 참여 기록을 생성합니다.
     * 이미 참여한 챌린지나 기간이 만료된 챌린지에는 참여할 수 없습니다.
     * 
     * @param challengeId 참여할 챌린지 ID
     * @param authentication 현재 인증된 사용자
     * @return 참여 기록 정보
     */
    @PostMapping("/{challengeId}/join")
    public ResponseEntity<ApiResponse<UserChallengeResponse>> joinChallenge(
            @PathVariable Long challengeId,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Challenge challenge = challengeService.getChallengeById(challengeId);
        
        UserChallenge userChallenge = userChallengeService.joinChallenge(currentUser, challenge);
        UserChallengeResponse response = mapToUserChallengeResponse(userChallenge);
        
        return ResponseEntity.ok(
            ApiResponse.success("챌린지에 참여했습니다.", response)
        );
    }
    
    /**
     * 사용자가 참여 중인 챌린지에서 탈퇴합니다.
     * 탈퇴하면 해당 챌린지의 진행률과 참여 기록이 삭제됩니다.
     * 이미 완료된 챌린지는 탈퇴할 수 없습니다.
     * 
     * @param challengeId 탈퇴할 챌린지 ID
     * @param authentication 현재 인증된 사용자
     * @return 탈퇴 결과 메시지
     */
    @DeleteMapping("/{challengeId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveChallenge(
            @PathVariable Long challengeId,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Challenge challenge = challengeService.getChallengeById(challengeId);
        
        userChallengeService.leaveChallenge(currentUser, challenge);
        
        return ResponseEntity.ok(
            ApiResponse.success("챌린지에서 탈퇴했습니다.", null)
        );
    }
    
    /**
     * 챌린지 진행률 업데이트
     */
    @PostMapping("/{challengeId}/progress")
    public ResponseEntity<ApiResponse<UserChallengeResponse>> updateProgress(
            @PathVariable Long challengeId,
            @RequestParam Integer progressValue,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Challenge challenge = challengeService.getChallengeById(challengeId);
        
        UserChallenge userChallenge = userChallengeService.updateProgress(currentUser, challenge, progressValue);
        UserChallengeResponse response = mapToUserChallengeResponse(userChallenge);
        
        return ResponseEntity.ok(
            ApiResponse.success("챌린지 진행률이 업데이트되었습니다.", response)
        );
    }
    
    /**
     * 내가 참여한 챌린지 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<UserChallengeResponse>>> getMyParticipatedChallenges(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        List<UserChallenge> userChallenges = userChallengeService.getUserChallenges(currentUser);
        List<UserChallengeResponse> responses = userChallenges.stream()
                .map(this::mapToUserChallengeResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("내가 참여한 챌린지를 조회했습니다.", responses)
        );
    }
    
    /**
     * 내가 완료한 챌린지 목록 조회
     */
    @GetMapping("/my/completed")
    public ResponseEntity<ApiResponse<List<UserChallengeResponse>>> getMyCompletedChallenges(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        List<UserChallenge> userChallenges = userChallengeService.getCompletedChallenges(currentUser);
        List<UserChallengeResponse> responses = userChallenges.stream()
                .map(this::mapToUserChallengeResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("내가 완료한 챌린지를 조회했습니다.", responses)
        );
    }
    
    /**
     * 챌린지 참여자 목록 조회
     */
    @GetMapping("/{challengeId}/participants")
    public ResponseEntity<ApiResponse<List<UserChallengeResponse>>> getChallengeParticipants(@PathVariable Long challengeId) {
        Challenge challenge = challengeService.getChallengeById(challengeId);
        List<UserChallenge> participants = userChallengeService.getChallengeParticipants(challenge);
        List<UserChallengeResponse> responses = participants.stream()
                .map(this::mapToUserChallengeResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("챌린지 참여자를 조회했습니다.", responses)
        );
    }
    
    /**
     * 챌린지 리더보드 조회
     */
    @GetMapping("/{challengeId}/leaderboard")
    public ResponseEntity<ApiResponse<List<UserChallengeResponse>>> getChallengeLeaderboard(
            @PathVariable Long challengeId,
            @RequestParam(defaultValue = "10") int limit) {
        
        Challenge challenge = challengeService.getChallengeById(challengeId);
        List<UserChallenge> leaderboard = userChallengeService.getChallengeLeaderboard(challenge, limit);
        List<UserChallengeResponse> responses = leaderboard.stream()
                .map(this::mapToUserChallengeResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("챌린지 리더보드를 조회했습니다.", responses)
        );
    }
    
    /**
     * 내 챌린지 순위 조회
     */
    @GetMapping("/{challengeId}/my-ranking")
    public ResponseEntity<ApiResponse<Integer>> getMyRanking(
            @PathVariable Long challengeId,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Challenge challenge = challengeService.getChallengeById(challengeId);
        
        int ranking = userChallengeService.getUserRanking(currentUser, challenge);
        
        return ResponseEntity.ok(
            ApiResponse.success("내 챌린지 순위를 조회했습니다.", ranking)
        );
    }
    
    /**
     * Challenge 엔티티를 ChallengeResponse DTO로 변환
     */
    private ChallengeResponse mapToChallengeResponse(Challenge challenge) {
        long participantCount = challengeService.getParticipantCount(challenge.getId());
        
        return ChallengeResponse.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .targetValue(challenge.getTargetValue())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .imageUrl(challenge.getImageUrl())
                .isActive(challenge.getIsActive())
                .participantCount(participantCount)
                .createdAt(challenge.getCreatedAt())
                .build();
    }
    
    /**
     * UserChallenge 엔티티를 UserChallengeResponse DTO로 변환
     */
    private UserChallengeResponse mapToUserChallengeResponse(UserChallenge userChallenge) {
        return UserChallengeResponse.builder()
                .id(userChallenge.getId())
                .userId(userChallenge.getUser().getId())
                .userNickname(userChallenge.getUser().getNickname())
                .challengeId(userChallenge.getChallenge().getId())
                .challengeTitle(userChallenge.getChallenge().getTitle())
                .targetValue(userChallenge.getChallenge().getTargetValue())
                .currentValue(userChallenge.getCurrentValue())
                .isCompleted(userChallenge.getIsCompleted())
                .completedAt(userChallenge.getCompletedAt())
                .createdAt(userChallenge.getCreatedAt())
                .build();
    }
    
    /**
     * 챌린지 응답 DTO
     */
    public static class ChallengeResponse {
        private final Long id;
        private final String title;
        private final String description;
        private final Integer targetValue;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final String imageUrl;
        private final Boolean isActive;
        private final Long participantCount;
        private final OffsetDateTime createdAt;
        
        private ChallengeResponse(Long id, String title, String description, Integer targetValue,
                                LocalDate startDate, LocalDate endDate, String imageUrl, Boolean isActive,
                                Long participantCount, OffsetDateTime createdAt) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.targetValue = targetValue;
            this.startDate = startDate;
            this.endDate = endDate;
            this.imageUrl = imageUrl;
            this.isActive = isActive;
            this.participantCount = participantCount;
            this.createdAt = createdAt;
        }
        
        public static ChallengeResponseBuilder builder() {
            return new ChallengeResponseBuilder();
        }
        
        // Getters
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public Integer getTargetValue() { return targetValue; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public String getImageUrl() { return imageUrl; }
        public Boolean getIsActive() { return isActive; }
        public Long getParticipantCount() { return participantCount; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        
        public static class ChallengeResponseBuilder {
            private Long id;
            private String title;
            private String description;
            private Integer targetValue;
            private LocalDate startDate;
            private LocalDate endDate;
            private String imageUrl;
            private Boolean isActive;
            private Long participantCount;
            private OffsetDateTime createdAt;
            
            public ChallengeResponseBuilder id(Long id) { this.id = id; return this; }
            public ChallengeResponseBuilder title(String title) { this.title = title; return this; }
            public ChallengeResponseBuilder description(String description) { this.description = description; return this; }
            public ChallengeResponseBuilder targetValue(Integer targetValue) { this.targetValue = targetValue; return this; }
            public ChallengeResponseBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
            public ChallengeResponseBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
            public ChallengeResponseBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
            public ChallengeResponseBuilder isActive(Boolean isActive) { this.isActive = isActive; return this; }
            public ChallengeResponseBuilder participantCount(Long participantCount) { this.participantCount = participantCount; return this; }
            public ChallengeResponseBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
            
            public ChallengeResponse build() {
                return new ChallengeResponse(id, title, description, targetValue, startDate, endDate, imageUrl, isActive, participantCount, createdAt);
            }
        }
    }
    
    /**
     * 사용자 챌린지 응답 DTO
     */
    public static class UserChallengeResponse {
        private final Long id;
        private final Long userId;
        private final String userNickname;
        private final Long challengeId;
        private final String challengeTitle;
        private final Integer targetValue;
        private final Integer currentValue;
        private final Boolean isCompleted;
        private final OffsetDateTime completedAt;
        private final OffsetDateTime createdAt;
        
        private UserChallengeResponse(Long id, Long userId, String userNickname, Long challengeId,
                                    String challengeTitle, Integer targetValue, Integer currentValue,
                                    Boolean isCompleted, OffsetDateTime completedAt, OffsetDateTime createdAt) {
            this.id = id;
            this.userId = userId;
            this.userNickname = userNickname;
            this.challengeId = challengeId;
            this.challengeTitle = challengeTitle;
            this.targetValue = targetValue;
            this.currentValue = currentValue;
            this.isCompleted = isCompleted;
            this.completedAt = completedAt;
            this.createdAt = createdAt;
        }
        
        public static UserChallengeResponseBuilder builder() {
            return new UserChallengeResponseBuilder();
        }
        
        // Getters
        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public String getUserNickname() { return userNickname; }
        public Long getChallengeId() { return challengeId; }
        public String getChallengeTitle() { return challengeTitle; }
        public Integer getTargetValue() { return targetValue; }
        public Integer getCurrentValue() { return currentValue; }
        public Boolean getIsCompleted() { return isCompleted; }
        public OffsetDateTime getCompletedAt() { return completedAt; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        
        public static class UserChallengeResponseBuilder {
            private Long id;
            private Long userId;
            private String userNickname;
            private Long challengeId;
            private String challengeTitle;
            private Integer targetValue;
            private Integer currentValue;
            private Boolean isCompleted;
            private OffsetDateTime completedAt;
            private OffsetDateTime createdAt;
            
            public UserChallengeResponseBuilder id(Long id) { this.id = id; return this; }
            public UserChallengeResponseBuilder userId(Long userId) { this.userId = userId; return this; }
            public UserChallengeResponseBuilder userNickname(String userNickname) { this.userNickname = userNickname; return this; }
            public UserChallengeResponseBuilder challengeId(Long challengeId) { this.challengeId = challengeId; return this; }
            public UserChallengeResponseBuilder challengeTitle(String challengeTitle) { this.challengeTitle = challengeTitle; return this; }
            public UserChallengeResponseBuilder targetValue(Integer targetValue) { this.targetValue = targetValue; return this; }
            public UserChallengeResponseBuilder currentValue(Integer currentValue) { this.currentValue = currentValue; return this; }
            public UserChallengeResponseBuilder isCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; return this; }
            public UserChallengeResponseBuilder completedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; return this; }
            public UserChallengeResponseBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
            
            public UserChallengeResponse build() {
                return new UserChallengeResponse(id, userId, userNickname, challengeId, challengeTitle, targetValue, currentValue, isCompleted, completedAt, createdAt);
            }
        }
    }
}
