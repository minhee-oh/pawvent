package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.domain.Hazard;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.HazardCategory;
import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.dto.HazardReportRequest;
import com.pawvent.pawventserver.dto.HazardResponse;
import com.pawvent.pawventserver.service.HazardService;
import com.pawvent.pawventserver.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/hazards")
@RequiredArgsConstructor
public class HazardController {

    private final HazardService hazardService;
    private final UserService userService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @PostMapping("/report")
    @Transactional
    public ResponseEntity<ApiResponse<HazardResponse>> reportHazard(
            @Valid @RequestBody HazardReportRequest request,
            Authentication authentication) {
        
        try {
            User user = userService.getCurrentUser(authentication);
            
            Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
            location.setSRID(4326);
            
            Hazard hazard = hazardService.reportHazard(
                user, 
                request.getCategory(), 
                request.getDescription(),
                location,
                request.getImageUrl()
            );
            
            // 트랜잭션 내에서 User의 nickname을 미리 초기화
            String reporterNickname = null;
            Long reporterId = null;
            try {
                // Repository를 통해 User ID를 직접 조회 (hazard.getUser() 호출 방지)
                Long userId = hazardService.getUserIdByHazardId(hazard.getId());
                if (userId != null) {
                    // UserService를 사용하여 User를 명시적으로 조회 (managed 상태로 로드)
                    User reporter = userService.getUserById(userId);
                    reporterId = reporter.getId();
                    reporterNickname = reporter.getNickname();
                }
            } catch (Exception e) {
                log.warn("Hazard {}의 User 정보를 가져오는 중 오류 발생: {}", hazard.getId(), e.getMessage());
            }
            
            HazardResponse hazardResponse = mapToHazardResponse(hazard, reporterId, reporterNickname);
            
            return ResponseEntity.ok(ApiResponse.success("위험 스팟이 성공적으로 신고되었습니다.", hazardResponse));
            
        } catch (Exception e) {
            log.error("위험 스팟 신고 중 오류 발생", e);
            log.error("에러 상세: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("위험 스팟 신고에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/nearby")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<HazardResponse>>> getNearbyHazards(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "radius", defaultValue = "1000") double radius) {
        
        try {
            List<Hazard> hazards = hazardService.getHazardsNearLocation(latitude, longitude, radius);
            
            // 트랜잭션 내에서 User의 nickname을 미리 초기화
            List<HazardResponse> hazardResponses = new java.util.ArrayList<>(hazards.size());
            for (Hazard hazard : hazards) {
                // User ID를 가져오기 위해 Repository를 통해 직접 조회 (hazard.getUser() 호출 방지)
                String reporterNickname = null;
                Long reporterId = null;
                try {
                    // Repository를 통해 User ID를 직접 조회
                    Long userId = hazardService.getUserIdByHazardId(hazard.getId());
                    if (userId != null) {
                        // UserService를 사용하여 User를 명시적으로 조회 (managed 상태로 로드)
                        User reporter = userService.getUserById(userId);
                        reporterId = reporter.getId();
                        reporterNickname = reporter.getNickname();
                    }
                } catch (Exception e) {
                    log.warn("Hazard {}의 User 정보를 가져오는 중 오류 발생: {}", hazard.getId(), e.getMessage());
                }
                
                hazardResponses.add(mapToHazardResponse(hazard, reporterId, reporterNickname));
            }
            
            return ResponseEntity.ok(ApiResponse.success("주변 위험 스팟을 조회했습니다.", hazardResponses));
            
        } catch (Exception e) {
            log.error("주변 위험 스팟 조회 중 오류 발생", e);
            log.error("에러 상세: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("주변 위험 스팟 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Hazard>>> getHazardsByCategory(
            @PathVariable HazardCategory category) {
        
        try {
            List<Hazard> hazards = hazardService.getHazardsByCategory(category);
            return ResponseEntity.ok(ApiResponse.success(hazards));
            
        } catch (Exception e) {
            log.error("카테고리별 위험 스팟 조회 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("위험 스팟 조회에 실패했습니다."));
        }
    }

    @PutMapping("/{hazardId}")
    @Transactional
    public ResponseEntity<ApiResponse<HazardResponse>> updateHazard(
            @PathVariable("hazardId") Long hazardId,
            @Valid @RequestBody HazardReportRequest request,
            Authentication authentication) {
        
        try {
            User user = userService.getCurrentUser(authentication);
            
            Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
            location.setSRID(4326);
            
            Hazard hazard = hazardService.updateHazard(
                hazardId,
                user,
                request.getCategory(),
                request.getDescription(),
                location,
                request.getImageUrl()
            );
            
            // 트랜잭션 내에서 User의 nickname을 미리 초기화
            String reporterNickname = null;
            Long reporterId = null;
            try {
                Long userId = hazardService.getUserIdByHazardId(hazard.getId());
                if (userId != null) {
                    User reporter = userService.getUserById(userId);
                    reporterId = reporter.getId();
                    reporterNickname = reporter.getNickname();
                }
            } catch (Exception e) {
                log.warn("Hazard {}의 User 정보를 가져오는 중 오류 발생: {}", hazard.getId(), e.getMessage());
            }
            
            HazardResponse hazardResponse = mapToHazardResponse(hazard, reporterId, reporterNickname);
            
            return ResponseEntity.ok(ApiResponse.success("위험 스팟이 성공적으로 수정되었습니다.", hazardResponse));
            
        } catch (IllegalArgumentException e) {
            log.warn("위험 스팟 수정 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("위험 스팟 수정 중 오류 발생", e);
            return ResponseEntity.status(500).body(ApiResponse.error("위험 스팟 수정에 실패했습니다: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{hazardId}")
    public ResponseEntity<ApiResponse<Void>> deleteHazard(
            @PathVariable("hazardId") Long hazardId,
            Authentication authentication) {
        
        try {
            User user = userService.getCurrentUser(authentication);
            hazardService.deleteHazard(hazardId, user);
            
            return ResponseEntity.ok(ApiResponse.success("위험 스팟이 삭제되었습니다.", null));
            
        } catch (IllegalArgumentException e) {
            log.warn("위험 스팟 삭제 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("위험 스팟 삭제 중 오류 발생", e);
            return ResponseEntity.status(500).body(ApiResponse.error("위험 스팟 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * Hazard 엔티티를 HazardResponse DTO로 변환
     * Point를 위도/경도로 변환하여 JSON 직렬화 문제 해결
     */
    private HazardResponse mapToHazardResponse(Hazard hazard) {
        return mapToHazardResponse(hazard, null, null);
    }
    
    /**
     * Hazard 엔티티를 HazardResponse DTO로 변환 (User 정보 미리 초기화된 버전)
     */
    private HazardResponse mapToHazardResponse(Hazard hazard, Long reporterId, String reporterNickname) {
        Double latitude = null;
        Double longitude = null;
        if (hazard.getLocation() != null) {
            Coordinate coord = hazard.getLocation().getCoordinate();
            latitude = coord.y; // y = latitude
            longitude = coord.x; // x = longitude
        }
        
        return HazardResponse.builder()
                .id(hazard.getId())
                .category(hazard.getCategory())
                .description(hazard.getDescription())
                .latitude(latitude)
                .longitude(longitude)
                .imageUrl(hazard.getImageUrl())
                .reporterId(reporterId)
                .reporterNickname(reporterNickname)
                .createdAt(hazard.getCreatedAt())
                .build();
    }
}

