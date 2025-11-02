package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.WalkRoute;
import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.dto.RouteCreateRequest;
import com.pawvent.pawventserver.dto.RouteResponse;
import com.pawvent.pawventserver.service.UserService;
import com.pawvent.pawventserver.service.WalkRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class WalkRouteController {

    private final WalkRouteService walkRouteService;
    private final UserService userService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> createRoute(
            @Valid @RequestBody RouteCreateRequest request,
            Authentication authentication) {
        
        try {
            User user = userService.getCurrentUser(authentication);
            
            // 좌표 배열을 LineString으로 변환
            Coordinate[] coordinates = request.getCoordinates().stream()
                    .map(coord -> new Coordinate(coord.getLongitude(), coord.getLatitude()))
                    .toArray(Coordinate[]::new);
            
            LineString lineString = geometryFactory.createLineString(coordinates);
            lineString.setSRID(4326);
            
            WalkRoute route = walkRouteService.saveWalkRoute(
                user,
                request.getName(),
                lineString,
                request.getDistance(),
                request.getDuration(),
                request.isShared()
            );
            
            RouteResponse routeResponse = mapToRouteResponse(route);
            return ResponseEntity.ok(ApiResponse.success("산책 루트가 저장되었습니다.", routeResponse));
            
        } catch (Exception e) {
            log.error("산책 루트 저장 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("산책 루트 저장에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getMyRoutes(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            List<WalkRoute> routes = walkRouteService.getUserRoutes(user);
            List<RouteResponse> routeResponses = routes.stream()
                    .map(this::mapToRouteResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("내 산책 경로를 조회했습니다.", routeResponses));
            
        } catch (Exception e) {
            log.error("내 산책 루트 조회 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("산책 루트 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/shared")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getSharedRoutes() {
        try {
            List<WalkRoute> routes = walkRouteService.getSharedRoutes();
            List<RouteResponse> routeResponses = routes.stream()
                    .map(this::mapToRouteResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success("공유 산책 경로를 조회했습니다.", routeResponses));
            
        } catch (Exception e) {
            log.error("공유 산책 루트 조회 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("공유 루트 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<ApiResponse<RouteResponse>> getRoute(@PathVariable Long routeId) {
        try {
            WalkRoute route = walkRouteService.getRouteById(routeId);
            RouteResponse routeResponse = mapToRouteResponse(route);
            return ResponseEntity.ok(ApiResponse.success("산책 경로를 조회했습니다.", routeResponse));
            
        } catch (Exception e) {
            log.error("산책 루트 조회 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("산책 루트 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/{routeId}")
    public ResponseEntity<ApiResponse<RouteResponse>> updateRoute(
            @PathVariable Long routeId,
            @RequestParam("name") String name,
            @RequestParam("isShared") boolean isShared,
            Authentication authentication) {
        
        try {
            User user = userService.getCurrentUser(authentication);
            WalkRoute route = walkRouteService.updateRoute(routeId, name, isShared);
            RouteResponse routeResponse = mapToRouteResponse(route);
            
            return ResponseEntity.ok(ApiResponse.success("산책 루트가 수정되었습니다.", routeResponse));
            
        } catch (Exception e) {
            log.error("산책 루트 수정 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("산책 루트 수정에 실패했습니다: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(
            @PathVariable Long routeId,
            Authentication authentication) {
        
        try {
            User user = userService.getCurrentUser(authentication);
            walkRouteService.deleteRoute(routeId);
            
            return ResponseEntity.ok(ApiResponse.success("산책 루트가 삭제되었습니다.", null));
            
        } catch (Exception e) {
            log.error("산책 루트 삭제 중 오류 발생", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("산책 루트 삭제에 실패했습니다."));
        }
    }
    
    /**
     * WalkRoute 엔티티를 RouteResponse DTO로 변환
     * LineString을 좌표 배열로 변환하여 JSON 직렬화 문제 해결
     */
    private RouteResponse mapToRouteResponse(WalkRoute walkRoute) {
        // LineString을 좌표 배열로 변환
        List<RouteResponse.CoordinateDto> coordinates = null;
        if (walkRoute.getRouteData() != null) {
            Coordinate[] coords = walkRoute.getRouteData().getCoordinates();
            coordinates = java.util.Arrays.stream(coords)
                    .map(coord -> new RouteResponse.CoordinateDto(coord.y, coord.x)) // y=latitude, x=longitude
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return RouteResponse.builder()
                .id(walkRoute.getId())
                .name(walkRoute.getName())
                .coordinates(coordinates)
                .distance(walkRoute.getDistance())
                .duration(walkRoute.getDuration())
                .isShared(walkRoute.isShared())
                .authorId(walkRoute.getUser() != null ? walkRoute.getUser().getId() : null)
                .authorNickname(walkRoute.getUser() != null ? walkRoute.getUser().getNickname() : null)
                .createdAt(walkRoute.getCreatedAt())
                .build();
    }
}

