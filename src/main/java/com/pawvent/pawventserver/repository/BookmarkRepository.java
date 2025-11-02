package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.Bookmark;
import com.pawvent.pawventserver.domain.Hazard;
import com.pawvent.pawventserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 북마크(즐겨찾기) 관련 데이터베이스 접근을 담당하는 레포지토리
 * 사용자의 위험요소 북마크를 관리하는 기능을 제공
 */
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    
    /**
     * 특정 사용자의 모든 북마크를 최신순으로 조회
     * @param user 사용자
     * @return 해당 사용자의 북마크 목록 (최신순)
     */
    @Query("SELECT b FROM Bookmark b WHERE b.user = :user ORDER BY b.createdAt DESC")
    List<Bookmark> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    /**
     * 특정 사용자가 특정 위험요소를 북마크했는지 확인
     * @param user 사용자
     * @param hazard 위험요소
     * @return 북마크 엔티티 (없으면 Empty)
     */
    Optional<Bookmark> findByUserAndHazard(User user, Hazard hazard);
    
    /**
     * 특정 사용자가 특정 위험요소를 북마크했는지 여부 확인
     * @param user 사용자
     * @param hazard 위험요소
     * @return 북마크 여부
     */
    boolean existsByUserAndHazard(User user, Hazard hazard);
    
    /**
     * 특정 위험요소의 모든 북마크 조회
     * @param hazard 위험요소
     * @return 해당 위험요소의 북마크 목록
     */
    List<Bookmark> findByHazard(Hazard hazard);
    
    /**
     * 특정 위험요소의 북마크 수 조회
     * @param hazard 위험요소
     * @return 해당 위험요소의 총 북마크 수
     */
    long countByHazard(Hazard hazard);
    
    /**
     * 특정 사용자의 북마크 수 조회
     * @param user 사용자
     * @return 해당 사용자의 총 북마크 수
     */
    long countByUser(User user);
    
    /**
     * 특정 위험요소의 북마크를 최신순으로 조회 (북마크한 사용자 목록)
     * @param hazard 위험요소
     * @return 해당 위험요소를 북마크한 사용자들의 북마크 목록
     */
    @Query("SELECT b FROM Bookmark b WHERE b.hazard = :hazard ORDER BY b.createdAt DESC")
    List<Bookmark> findByHazardOrderByCreatedAtDesc(@Param("hazard") Hazard hazard);
    
    /**
     * 가장 많이 북마크된 위험요소들 조회 (주목받는 위험요소)
     * @return 북마크 수가 많은 위험요소들의 북마크 목록
     */
    @Query("SELECT b.hazard, COUNT(b) as bookmarkCount FROM Bookmark b " +
           "GROUP BY b.hazard ORDER BY COUNT(b) DESC")
    List<Object[]> findPopularHazards();
    
    /**
     * 특정 사용자와 위험요소로 북마크 삭제
     * @param user 사용자
     * @param hazard 위험요소
     */
    void deleteByUserAndHazard(User user, Hazard hazard);
    
    /**
     * 특정 위험요소의 모든 북마크 삭제
     * @param hazard 위험요소
     */
    void deleteByHazard(Hazard hazard);
    
    /**
     * 특정 사용자의 모든 북마크 삭제
     * @param user 사용자
     */
    void deleteByUser(User user);
}
