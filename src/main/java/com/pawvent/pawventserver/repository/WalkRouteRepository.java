package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.WalkRoute;
import com.pawvent.pawventserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalkRouteRepository extends JpaRepository<WalkRoute, Long> {
    
    List<WalkRoute> findByUser(User user);
    List<WalkRoute> findByIsSharedTrue();
    List<WalkRoute> findByUserAndIsShared(User user, boolean isShared);
    
    @Query("SELECT w FROM WalkRoute w WHERE w.isShared = true ORDER BY w.createdAt DESC")
    List<WalkRoute> findSharedRoutesOrderByCreatedDesc();
}
