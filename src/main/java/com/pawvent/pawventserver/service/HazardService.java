package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.Hazard;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.HazardCategory;
import com.pawvent.pawventserver.repository.HazardRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HazardService {
    
    private final HazardRepository hazardRepository;
    
    @Transactional
    public Hazard reportHazard(User user, HazardCategory category, String description, Point location, String imageUrl) {
        Hazard hazard = Hazard.builder()
                .user(user)
                .category(category)
                .description(description)
                .location(location)
                .imageUrl(imageUrl)
                .build();
        
        return hazardRepository.save(hazard);
    }
    
    public List<Hazard> getHazardsNearLocation(double latitude, double longitude, double radiusInMeters) {
        return hazardRepository.findHazardsNearLocation(latitude, longitude, radiusInMeters);
    }
    
    public Long getUserIdByHazardId(Long hazardId) {
        return hazardRepository.findUserIdByHazardId(hazardId);
    }
    
    public List<Hazard> getHazardsByCategory(HazardCategory category) {
        return hazardRepository.findByCategory(category);
    }
    
    public Hazard getHazardById(Long hazardId) {
        return hazardRepository.findById(hazardId)
                .orElseThrow(() -> new IllegalArgumentException("위험 요소를 찾을 수 없습니다."));
    }
    
    @Transactional
    public Hazard updateHazard(Long hazardId, User user, HazardCategory category, String description, Point location, String imageUrl) {
        Hazard hazard = getHazardById(hazardId);
        
        // 권한 체크: 본인이 신고한 위험 스팟만 수정 가능
        Long reporterUserId = getUserIdByHazardId(hazardId);
        if (reporterUserId == null || !reporterUserId.equals(user.getId())) {
            throw new IllegalArgumentException("위험 스팟을 수정할 권한이 없습니다.");
        }
        
        hazard.setCategory(category);
        hazard.setDescription(description);
        hazard.setLocation(location);
        hazard.setImageUrl(imageUrl);
        
        return hazardRepository.save(hazard);
    }
    
    @Transactional
    public void deleteHazard(Long hazardId, User user) {
        Hazard hazard = getHazardById(hazardId);
        
        // 권한 체크: 본인이 신고한 위험 스팟만 삭제 가능
        Long reporterUserId = getUserIdByHazardId(hazardId);
        if (reporterUserId == null || !reporterUserId.equals(user.getId())) {
            throw new IllegalArgumentException("위험 스팟을 삭제할 권한이 없습니다.");
        }
        
        hazard.setDeletedAt(OffsetDateTime.now());
        hazardRepository.save(hazard);
    }
    
    /**
     * 주어진 경로에 위험 요소가 있는지 확인
     */
    public boolean hasHazardOnRoute(double startLat, double startLng, double endLat, double endLng, double bufferMeters) {
        // 경로 중점 계산
        double midLat = (startLat + endLat) / 2;
        double midLng = (startLng + endLng) / 2;
        
        // 경로 길이 기반으로 검색 반경 확장
        double distance = calculateDistance(startLat, startLng, endLat, endLng);
        double searchRadius = Math.max(bufferMeters, distance / 2 + bufferMeters);
        
        List<Hazard> nearbyHazards = getHazardsNearLocation(midLat, midLng, searchRadius);
        return !nearbyHazards.isEmpty();
    }
    
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
