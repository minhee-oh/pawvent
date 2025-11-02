package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.dto.WalkSessionCreateRequest;
import com.pawvent.pawventserver.dto.WalkSessionResponse;
import com.pawvent.pawventserver.domain.Pet;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.WalkRoute;
import com.pawvent.pawventserver.domain.WalkSession;
import com.pawvent.pawventserver.service.PetService;
import com.pawvent.pawventserver.service.UserService;
import com.pawvent.pawventserver.service.WalkRouteService;
import com.pawvent.pawventserver.service.WalkSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 산책 세션 관리 컨트롤러
 * 
 * 산책 세션의 시작, 완료, 조회, 통계 등의 기능을 제공합니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/api/walk-sessions")
@RequiredArgsConstructor
public class WalkSessionController {
    
    private final WalkSessionService walkSessionService;
    private final UserService userService;
    private final PetService petService;
    private final WalkRouteService walkRouteService;
    
    /**
     * 새로운 산책 세션을 시작합니다.
     * 사용자가 특정 반려동물과 함께 선택된 경로로 산책을 시작할 때 사용합니다.
     * 시작 시간이 자동으로 기록되며, 산책 진행 상태가 됩니다.
     * 
     * @param request 산책 세션 생성 요청 데이터 (반려동물 ID, 경로 ID)
     * @param authentication 현재 인증된 사용자
     * @return 생성된 산책 세션 정보
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<WalkSessionResponse>> startWalkSession(
            @Valid @RequestBody WalkSessionCreateRequest request,
            Authentication authentication) {
        
        try {
            log.info("산책 세션 시작 요청: petId={}, routeId={}", request.getPetId(), request.getRouteId());
            
            User currentUser = userService.getCurrentUser(authentication);
            log.debug("현재 사용자: userId={}, nickname={}", currentUser.getId(), currentUser.getNickname());
            
            // Pet 확인 및 생성 (없는 경우)
            Pet pet;
            try {
                pet = petService.getPetById(request.getPetId());
                log.debug("Pet 조회 성공: petId={}, name={}", pet.getId(), pet.getName());
            } catch (IllegalArgumentException e) {
                log.warn("Pet 조회 실패 (petId={}), 기본 Pet 생성 시도", request.getPetId());
                // Pet이 없으면 현재 사용자의 첫 번째 Pet을 가져오거나 생성
                pet = getOrCreateDefaultPet(currentUser, request.getPetId());
                log.info("기본 Pet 생성/조회 완료: petId={}, name={}", pet.getId(), pet.getName());
            }
            
            // Route 확인 (없는 경우 null 허용)
            WalkRoute route = null;
            if (request.getRouteId() != null) {
                try {
                    route = walkRouteService.getRouteById(request.getRouteId());
                    log.debug("Route 조회 성공: routeId={}, name={}", route.getId(), route.getName());
                } catch (IllegalArgumentException e) {
                    log.warn("Route 조회 실패 (routeId={}), route 없이 진행", request.getRouteId());
                    // Route가 없어도 산책은 시작 가능 (선택사항)
                    route = null;
                }
            }
            
            WalkSession walkSession = walkSessionService.startWalkSession(currentUser, pet, route);
            log.info("산책 세션 생성 완료: sessionId={}", walkSession.getId());
            
            WalkSessionResponse sessionResponse = mapToWalkSessionResponse(walkSession);
            
            return ResponseEntity.ok(
                ApiResponse.success("산책 세션이 시작되었습니다.", sessionResponse)
            );
        } catch (Exception e) {
            log.error("산책 세션 시작 중 오류 발생", e);
            // 개발 환경에서 상세한 에러 정보 제공
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getSimpleName();
            }
            // 스택 트레이스의 첫 번째 줄도 포함
            if (e.getCause() != null) {
                errorMessage += " (원인: " + e.getCause().getMessage() + ")";
            }
            return ResponseEntity.status(500).body(
                ApiResponse.error("산책 세션 시작에 실패했습니다: " + errorMessage)
            );
        }
    }
    
    /**
     * 기본 Pet을 조회하거나 생성합니다 (개발용)
     */
    private Pet getOrCreateDefaultPet(User user, Long requestedPetId) {
        // 먼저 사용자의 Pet 목록 확인
        List<Pet> userPets = petService.getPetsByUser(user);
        if (!userPets.isEmpty()) {
            return userPets.get(0);
        }
        
        // Pet이 없으면 더미 Pet 생성
        return petService.createPet(user, "내 반려동물", "강아지", null, 
                null, null, 0.0, null, null);
    }
    
    /**
     * 진행 중인 산책 세션을 완료하고 결과 데이터를 저장합니다.
     * 실제 산책한 거리, 시간, 칼로리를 기록하고 사진과 메모를 추가할 수 있습니다.
     * 세션 주인만 완료할 수 있으며, 완료 후에도 정보 수정이 가능합니다.
     * 
     * @param sessionId 완료할 산책 세션의 ID
     * @param distance 실제 산책한 거리 (미터)
     * @param duration 산책에 소요된 시간 (초)
     * @param calories 번 칼로리
     * @param imageUrls 산책 중 촬영한 사진 URL 목록 (선택사항)
     * @param memo 산책 후기 또는 메모 (선택사항)
     * @param authentication 현재 인증된 사용자
     * @return 완료된 산책 세션 정보
     */
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<WalkSessionResponse>> completeWalkSession(
            @PathVariable Long sessionId,
            @RequestParam Double distance,
            @RequestParam Integer duration,
            @RequestParam Integer calories,
            @RequestParam(required = false) List<String> imageUrls,
            @RequestParam(required = false) String memo,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        
        WalkSession completedSession = walkSessionService.completeWalkSession(
            sessionId, currentUser, distance, duration
        );
        
        WalkSessionResponse sessionResponse = mapToWalkSessionResponse(completedSession);
        
        return ResponseEntity.ok(
            ApiResponse.success("산책 세션이 완료되었습니다.", sessionResponse)
        );
    }
    
    /**
     * 산책 세션을 취소합니다 (소프트 삭제).
     * 실수로 생성한 세션이나 더 이상 필요하지 않은 세션을 취소할 때 사용합니다.
     * 세션 주인만 취소할 수 있습니다.
     * 
     * @param sessionId 취소할 산책 세션 ID
     * @param authentication 현재 인증된 사용자
     * @return 취소 결과 메시지
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> cancelWalkSession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        walkSessionService.cancelWalkSession(sessionId, currentUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("산책 세션이 취소되었습니다.", null)
        );
    }
    
    /**
     * 현재 인증된 사용자의 모든 산책 세션 목록을 조회합니다.
     * 삭제되지 않은 세션만 최신순으로 반환합니다.
     * 
     * @param authentication 현재 인증된 사용자
     * @return 사용자의 산책 세션 목록
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<WalkSessionResponse>>> getMyWalkSessions(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        List<WalkSession> walkSessions = walkSessionService.getWalkSessionsByUser(currentUser);
        List<WalkSessionResponse> sessionResponses = walkSessions.stream()
                .map(this::mapToWalkSessionResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("내 산책 세션 목록을 조회했습니다.", sessionResponses)
        );
    }
    
    /**
     * 현재 인증된 사용자의 산책 세션 목록을 페이지 단위로 조회합니다.
     * 대량의 데이터를 효율적으로 처리하기 위해 페이지다이션을 사용합니다.
     * 
     * @param pageable 페이지 정보 (페이지 번호, 사이즈, 정렬)
     * @param authentication 현재 인증된 사용자
     * @return 페이지너이션된 산책 세션 목록
     */
    @GetMapping("/my/paged")
    public ResponseEntity<ApiResponse<Page<WalkSessionResponse>>> getMyWalkSessionsPaged(
            Pageable pageable,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Page<WalkSession> walkSessions = walkSessionService.getWalkSessionsByUser(currentUser, pageable);
        Page<WalkSessionResponse> sessionResponses = walkSessions.map(this::mapToWalkSessionResponse);
        
        return ResponseEntity.ok(
            ApiResponse.success("내 산책 세션 목록을 조회했습니다.", sessionResponses)
        );
    }
    
    /**
     * 현재 인증된 사용자의 완료된 산책 세션만 조회합니다.
     * 산책 기록이나 통계를 확인할 때 사용합니다.
     * 
     * @param authentication 현재 인증된 사용자
     * @return 완료된 산책 세션 목록
     */
    @GetMapping("/my/completed")
    public ResponseEntity<ApiResponse<List<WalkSessionResponse>>> getMyCompletedSessions(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        List<WalkSession> walkSessions = walkSessionService.getCompletedWalkSessions(currentUser);
        List<WalkSessionResponse> sessionResponses = walkSessions.stream()
                .map(this::mapToWalkSessionResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("완료된 산책 세션을 조회했습니다.", sessionResponses)
        );
    }
    
    /**
     * 현재 인증된 사용자의 진행 중인 산책 세션만 조회합니다.
     * 아직 완료되지 않은 산책 세션을 조회하여 재개하거나 완료할 수 있습니다.
     * 
     * @param authentication 현재 인증된 사용자
     * @return 진행 중인 산책 세션 목록
     */
    @GetMapping("/my/active")
    public ResponseEntity<ApiResponse<List<WalkSessionResponse>>> getMyActiveSessions(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        List<WalkSession> walkSessions = walkSessionService.getActiveWalkSessions(currentUser);
        List<WalkSessionResponse> sessionResponses = walkSessions.stream()
                .map(this::mapToWalkSessionResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("진행 중인 산책 세션을 조회했습니다.", sessionResponses)
        );
    }
    
    /**
     * 특정 날짜에 진행한 산책 세션들을 조회합니다.
     * 달력이나 일일 보고서에서 사용할 수 있습니다.
     * 
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @param authentication 현재 인증된 사용자
     * @return 해당 날짜의 산책 세션 목록
     */
    @GetMapping("/my/date/{date}")
    public ResponseEntity<ApiResponse<List<WalkSessionResponse>>> getWalkSessionsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        List<WalkSession> walkSessions = walkSessionService.getWalkSessionsByDate(currentUser, date);
        List<WalkSessionResponse> sessionResponses = walkSessions.stream()
                .map(this::mapToWalkSessionResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success(date + "의 산책 세션을 조회했습니다.", sessionResponses)
        );
    }
    
    /**
     * 특정 기간의 산책 세션 조회
     */
    @GetMapping("/my/range")
    public ResponseEntity<ApiResponse<List<WalkSessionResponse>>> getWalkSessionsByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        List<WalkSession> walkSessions = walkSessionService.getWalkSessionsByDateRange(currentUser, startDate, endDate);
        List<WalkSessionResponse> sessionResponses = walkSessions.stream()
                .map(this::mapToWalkSessionResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("기간별 산책 세션을 조회했습니다.", sessionResponses)
        );
    }
    
    /**
     * 특정 산책 세션 조회
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<WalkSessionResponse>> getWalkSession(@PathVariable Long sessionId) {
        WalkSession walkSession = walkSessionService.getWalkSessionById(sessionId);
        WalkSessionResponse sessionResponse = mapToWalkSessionResponse(walkSession);
        
        return ResponseEntity.ok(
            ApiResponse.success("산책 세션을 조회했습니다.", sessionResponse)
        );
    }
    
    /**
     * 최근 산책 세션 조회
     */
    @GetMapping("/my/recent")
    public ResponseEntity<ApiResponse<List<WalkSessionResponse>>> getRecentWalkSessions(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        List<WalkSession> walkSessions = walkSessionService.getRecentWalkSessions(currentUser, limit);
        List<WalkSessionResponse> sessionResponses = walkSessions.stream()
                .map(this::mapToWalkSessionResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("최근 산책 세션을 조회했습니다.", sessionResponses)
        );
    }
    
    /**
     * 산책 통계 조회
     */
    @GetMapping("/my/stats")
    public ResponseEntity<ApiResponse<WalkSessionService.WalkStats>> getMyWalkStats(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        WalkSessionService.WalkStats stats = walkSessionService.getUserWalkStats(currentUser, startDate, endDate);
        
        return ResponseEntity.ok(
            ApiResponse.success("산책 통계를 조회했습니다.", stats)
        );
    }
    
    /**
     * 월별 산책 통계 조회
     */
    @GetMapping("/my/stats/monthly/{year}/{month}")
    public ResponseEntity<ApiResponse<WalkSessionService.WalkStats>> getMonthlyStats(
            @PathVariable int year,
            @PathVariable int month,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        WalkSessionService.WalkStats stats = walkSessionService.getUserMonthlyStats(currentUser, year, month);
        
        return ResponseEntity.ok(
            ApiResponse.success(year + "년 " + month + "월 산책 통계를 조회했습니다.", stats)
        );
    }
    
    /**
     * WalkSession 엔티티를 WalkSessionResponse DTO로 변환
     */
    private WalkSessionResponse mapToWalkSessionResponse(WalkSession walkSession) {
        if (walkSession == null) {
            log.error("walkSession이 null입니다");
            throw new IllegalArgumentException("산책 세션 정보가 없습니다.");
        }
        
        return WalkSessionResponse.builder()
                .id(walkSession.getId())
                .userId(walkSession.getUser() != null ? walkSession.getUser().getId() : null)
                .userNickname(walkSession.getUser() != null ? walkSession.getUser().getNickname() : null)
                .petId(walkSession.getPet() != null ? walkSession.getPet().getId() : null)
                .petName(walkSession.getPet() != null ? walkSession.getPet().getName() : null)
                .routeId(walkSession.getRoute() != null ? walkSession.getRoute().getId() : null)
                .routeName(walkSession.getRoute() != null ? walkSession.getRoute().getName() : null)
                .startTime(walkSession.getStartTime())
                .endTime(walkSession.getEndTime())
                .distance(walkSession.getActualDistance())
                .duration(walkSession.getActualDuration())
                .isCompleted(walkSession.getIsCompleted())
                .createdAt(walkSession.getCreatedAt())
                .build();
    }
}
