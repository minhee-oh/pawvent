package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.WalkRoute;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.repository.WalkRouteRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkRouteService {
    
    private final WalkRouteRepository walkRouteRepository;
    
    @Transactional
    public WalkRoute saveWalkRoute(User user, String name, LineString routeData, Double distance, Integer duration, boolean isShared) {
        WalkRoute walkRoute = WalkRoute.builder()
                .user(user)
                .name(name)
                .routeData(routeData)
                .distance(distance)
                .duration(duration)
                .isShared(isShared)
                .build();
        
        return walkRouteRepository.save(walkRoute);
    }
    
    public List<WalkRoute> getUserRoutes(User user) {
        return walkRouteRepository.findByUser(user);
    }
    
    public List<WalkRoute> getSharedRoutes() {
        return walkRouteRepository.findSharedRoutesOrderByCreatedDesc();
    }
    
    public WalkRoute getRouteById(Long routeId) {
        return walkRouteRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("산책 경로를 찾을 수 없습니다."));
    }
    
    @Transactional
    public WalkRoute updateRoute(Long routeId, String name, boolean isShared) {
        WalkRoute route = walkRouteRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));
        
        route.setName(name);
        route.setShared(isShared);
        
        return walkRouteRepository.save(route);
    }
    
    @Transactional
    public void deleteRoute(Long routeId) {
        walkRouteRepository.deleteById(routeId);
    }
}
