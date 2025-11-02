package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.service.EmergencyResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyResponseService emergencyResponseService;

    @PostMapping("/report")
    public ResponseEntity<ApiResponse<EmergencyResponseService.EmergencyResponse>> reportEmergency(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam String emergencyType) {
        
        try {
            EmergencyResponseService.EmergencyResponse response = 
                emergencyResponseService.handleEmergency(latitude, longitude, emergencyType);
            
            return ResponseEntity.ok(ApiResponse.success("응급상황이 접수되었습니다.", response));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("응급상황 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/safe-route")
    public ResponseEntity<ApiResponse<EmergencyResponseService.SafeRouteRecommendation>> getSafeRoute(
            @RequestParam double startLat,
            @RequestParam double startLng,
            @RequestParam double endLat,
            @RequestParam double endLng) {
        
        try {
            EmergencyResponseService.SafeRouteRecommendation recommendation = 
                emergencyResponseService.recommendSafeRoute(startLat, startLng, endLat, endLng);
            
            return ResponseEntity.ok(ApiResponse.success(recommendation));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("안전 경로 조회 중 오류가 발생했습니다."));
        }
    }
}

