package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.Pet;
import com.pawvent.pawventserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 펫(반려동물) 관련 데이터베이스 접근을 담당하는 레포지토리
 * 사용자의 반려동물 정보 조회, 저장, 수정하는 기능을 제공
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    /**
     * 특정 사용자의 모든 펫 조회
     * @param user 조회할 사용자
     * @return 해당 사용자의 펫 목록
     */
    List<Pet> findByUser(User user);
    
    /**
     * 특정 사용자의 활성 펫을 최신순으로 조회
     */
    List<Pet> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(User user);
    
    /**
     * 모든 활성 펫을 최신순으로 조회
     */
    List<Pet> findByDeletedAtIsNullOrderByCreatedAtDesc();
    
    /**
     * 특정 종의 활성 펫들을 최신순으로 조회
     */
    List<Pet> findBySpeciesAndDeletedAtIsNullOrderByCreatedAtDesc(String species);
    
    /**
     * 특정 품종의 활성 펫들을 최신순으로 조회
     */
    List<Pet> findByBreedAndDeletedAtIsNullOrderByCreatedAtDesc(String breed);
    
    /**
     * 특정 사용자의 펫을 등록일순으로 조회
     * @param user 조회할 사용자
     * @return 해당 사용자의 펫 목록 (등록일 오름차순)
     */
    @Query("SELECT p FROM Pet p WHERE p.user = :user ORDER BY p.createdAt ASC")
    List<Pet> findByUserOrderByCreatedAtAsc(@Param("user") User user);
    
    /**
     * 특정 종의 펫들 조회
     * @param user 조회할 사용자
     * @param species 종 (예: "강아지", "고양이")
     * @return 해당 종의 펫 목록
     */
    List<Pet> findByUserAndSpecies(User user, String species);
    
    /**
     * 펫 이름으로 검색 (부분 일치)
     * @param user 조회할 사용자
     * @param name 검색할 펫 이름
     * @return 이름이 일치하는 펫 목록
     */
    @Query("SELECT p FROM Pet p WHERE p.user = :user AND p.name LIKE CONCAT('%', :name, '%')")
    List<Pet> findByUserAndNameContaining(@Param("user") User user, @Param("name") String name);
    
    /**
     * 특정 견종의 펫들 조회
     * @param user 조회할 사용자
     * @param breed 견종
     * @return 해당 견종의 펫 목록
     */
    List<Pet> findByUserAndBreed(User user, String breed);
    
    /**
     * 사용자의 펫 개수 조회
     * @param user 조회할 사용자
     * @return 해당 사용자의 총 펫 개수
     */
    long countByUser(User user);
    
    /**
     * 사용자의 활성 펫 개수 조회
     */
    long countByUserAndDeletedAtIsNull(User user);
    
    /**
     * 전체 활성 펫 개수 조회
     */
    long countByDeletedAtIsNull();
    
    /**
     * 특정 종의 활성 펫 개수 조회
     */
    long countBySpeciesAndDeletedAtIsNull(String species);
    
    /**
     * 특정 날짜 이전에 태어난 펫 개수 조회 (나이 계산용)
     * @param user 조회할 사용자
     * @param date 기준 날짜
     * @return 해당 조건의 펫 개수
     */
    @Query("SELECT COUNT(p) FROM Pet p WHERE p.user = :user AND p.birthDate <= :date AND p.deletedAt IS NULL")
    long countByUserAndBirthDateBefore(@Param("user") User user, @Param("date") java.time.LocalDate date);
}
