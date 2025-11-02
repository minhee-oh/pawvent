package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.Hazard;
import com.pawvent.pawventserver.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyResponseService {
    
    private final HazardService hazardService;
    
    /**
     * 위험 상황 발생 시 대응 서비스
     * 사용자 위치 주변의 위험 요소를 확인하고 알림을 제공
     */
    public EmergencyResponse handleEmergency(double latitude, double longitude, String emergencyType) {
        log.info("위험 상황 발생: 타입={}, 위치=({}, {})", emergencyType, latitude, longitude);
        
        EmergencyResponse response = new EmergencyResponse();
        response.setLatitude(latitude);
        response.setLongitude(longitude);
        response.setEmergencyType(emergencyType);
        
        // 주변 위험 요소 확인 (반경 500m)
        List<Hazard> nearbyHazards = hazardService.getHazardsNearLocation(latitude, longitude, 500.0);
        
        if (!nearbyHazards.isEmpty()) {
            response.setHasNearbyHazards(true);
            response.setNearbyHazardCount(nearbyHazards.size());
            response.setRecommendation("주변에 " + nearbyHazards.size() + "개의 위험 요소가 있습니다. 안전한 경로로 이동하세요.");
        } else {
            response.setHasNearbyHazards(false);
            response.setRecommendation("현재 위치는 상대적으로 안전합니다. 가까운 안전 지역으로 이동하세요.");
        }
        
        // 응급상황별 대응 가이드 제공
        response.setEmergencyGuide(getEmergencyGuide(emergencyType));
        
        return response;
    }
    
    /**
     * 안전한 대체 경로 제안
     */
    public SafeRouteRecommendation recommendSafeRoute(double startLat, double startLng, double endLat, double endLng) {
        SafeRouteRecommendation recommendation = new SafeRouteRecommendation();
        
        // 직선 경로의 위험 요소 확인
        boolean hasHazardOnDirectRoute = hazardService.hasHazardOnRoute(startLat, startLng, endLat, endLng, 100.0);
        
        if (hasHazardOnDirectRoute) {
            recommendation.setHasAlternativeRoute(true);
            recommendation.setReason("직선 경로에 위험 요소가 발견되었습니다.");
            recommendation.setRecommendation("우회 경로를 이용하시거나 다른 시간에 이동하는 것을 권장합니다.");
        } else {
            recommendation.setHasAlternativeRoute(false);
            recommendation.setRecommendation("현재 경로는 안전합니다.");
        }
        
        return recommendation;
    }
    
    private String getEmergencyGuide(String emergencyType) {
        switch (emergencyType.toUpperCase()) {
            case "AGGRESSIVE_DOG":
                return """
                공격적인 개 대응 가이드:
                1. 급작스러운 움직임을 피하고 천천히 뒤로 물러나세요
                2. 직접적인 눈 맞춤을 피하세요
                3. 큰 소리를 내지 말고 침착하게 행동하세요
                4. 물건으로 자신을 보호하고 119에 신고하세요
                """;
            case "TRAFFIC_ACCIDENT":
                return """
                교통사고 대응 가이드:
                1. 즉시 안전한 장소로 이동하세요
                2. 119와 112에 신고하세요
                3. 부상자가 있다면 응급처치를 실시하세요
                4. 사고 현장을 보존하고 사진을 촬영하세요
                """;
            case "LOST_PET":
                return """
                반려동물 실종 대응 가이드:
                1. 즉시 주변을 수색하며 반려동물 이름을 불러보세요
                2. 최근 방문했던 장소들을 확인하세요
                3. 지역 동물보호소와 경찰서에 신고하세요
                4. SNS와 커뮤니티에 실종 공고를 올리세요
                """;
            default:
                return """
                일반 응급상황 대응:
                1. 침착함을 유지하고 상황을 파악하세요
                2. 필요시 119에 신고하세요
                3. 안전한 장소로 이동하세요
                4. 주변 사람들에게 도움을 요청하세요
                """;
        }
    }
    
    // DTO 클래스들
    public static class EmergencyResponse {
        private double latitude;
        private double longitude;
        private String emergencyType;
        private boolean hasNearbyHazards;
        private int nearbyHazardCount;
        private String recommendation;
        private String emergencyGuide;
        
        // getters and setters
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        public String getEmergencyType() { return emergencyType; }
        public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }
        public boolean isHasNearbyHazards() { return hasNearbyHazards; }
        public void setHasNearbyHazards(boolean hasNearbyHazards) { this.hasNearbyHazards = hasNearbyHazards; }
        public int getNearbyHazardCount() { return nearbyHazardCount; }
        public void setNearbyHazardCount(int nearbyHazardCount) { this.nearbyHazardCount = nearbyHazardCount; }
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
        public String getEmergencyGuide() { return emergencyGuide; }
        public void setEmergencyGuide(String emergencyGuide) { this.emergencyGuide = emergencyGuide; }
    }
    
    public static class SafeRouteRecommendation {
        private boolean hasAlternativeRoute;
        private String reason;
        private String recommendation;
        
        public boolean isHasAlternativeRoute() { return hasAlternativeRoute; }
        public void setHasAlternativeRoute(boolean hasAlternativeRoute) { this.hasAlternativeRoute = hasAlternativeRoute; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }
}

