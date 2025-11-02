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
    public ResponseEntity<ApiResponse<Hazard>> reportHazard(
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
            
            return ResponseEntity.ok(ApiResponse.success("위험 스팟이 성공적으로 신고되었습니다.", hazard));
            
        } catch (Exception e) {
            log.error("위험 스팟 신고 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("위험 스팟 신고에 실패했습니다."));
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<HazardResponse>>> getNearbyHazards(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "radius", defaultValue = "1000") double radius) {
        
        try {
            List<Hazard> hazards = hazardService.getHazardsNearLocation(latitude, longitude, radius);
            List<HazardResponse> hazardResponses = hazards.stream()
                    .map(this::mapToHazardResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success("주변 위험 스팟을 조회했습니다.", hazardResponses));
            
        } catch (Exception e) {
            log.error("주변 위험 스팟 조회 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("주변 위험 스팟 조회에 실패했습니다: " + e.getMessage()));
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

    @DeleteMapping("/{hazardId}")
    public ResponseEntity<ApiResponse<Void>> deleteHazard(
            @PathVariable Long hazardId,
            Authentication authentication) {
        
        try {
            User user = userService.getCurrentUser(authentication);
            hazardService.deleteHazard(hazardId);
            
            return ResponseEntity.ok(ApiResponse.success("위험 스팟이 삭제되었습니다.", null));
            
        } catch (Exception e) {
            log.error("위험 스팟 삭제 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("위험 스팟 삭제에 실패했습니다."));
        }
    }
    
    /**
     * Hazard 엔티티를 HazardResponse DTO로 변환
     * Point를 위도/경도로 변환하여 JSON 직렬화 문제 해결
     */
    private HazardResponse mapToHazardResponse(Hazard hazard) {
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
                .reporterId(hazard.getUser() != null ? hazard.getUser().getId() : null)
                .reporterNickname(hazard.getUser() != null ? hazard.getUser().getNickname() : null)
                .createdAt(hazard.getCreatedAt())
                .build();
    }
}

