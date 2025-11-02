package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.Hazard;
import com.pawvent.pawventserver.domain.enums.HazardCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HazardRepository extends JpaRepository<Hazard, Long> {
    
    List<Hazard> findByCategory(HazardCategory category);
    
    @Query(value = "SELECT * FROM hazard h WHERE h.deleted_at IS NULL " +
            "AND ST_DWithin(h.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radiusInMeters)",
            nativeQuery = true)
    List<Hazard> findHazardsNearLocation(
            @Param("latitude") double latitude, 
            @Param("longitude") double longitude, 
            @Param("radiusInMeters") double radiusInMeters
    );
}
