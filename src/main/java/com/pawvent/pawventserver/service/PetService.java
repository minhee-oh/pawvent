package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.Pet;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {
    
    private final PetRepository petRepository;
    
    /**
     * 새로운 반려동물을 등록합니다.
     * 사용자는 자신의 반려동물 정보를 등록하여 산책 기록과 건강 관리를 할 수 있습니다.
     * 필수 정보(이름, 종)와 선택 정보(생년월일, 무게, 설명 등)를 입력할 수 있습니다.
     * 
     * @param user 반려동물의 소유주
     * @param name 반려동물 이름
     * @param species 동물 종류 (개, 고양이 등)
     * @param breed 품종
     * @param birthDate 생년월일
     * @param gender 성별
     * @param weight 무게 (kg)
     * @param imageUrl 프로필 이미지 URL
     * @param description 반려동물 소개 및 특이사항
     * @return 등록된 반려동물 엔티티
     */
    @Transactional
    public Pet createPet(User user, String name, String species, String breed, 
                        LocalDate birthDate, String gender, Double weight, 
                        String imageUrl, String description) {
        Pet pet = Pet.builder()
                .user(user)
                .name(name)
                .species(species)
                .breed(breed)
                .birthDate(birthDate)
                .gender(gender)
                .weight(weight)
                .imageUrl(imageUrl)
                .description(description)
                .build();
        
        return petRepository.save(pet);
    }
    
    /**
     * ID로 특정 반려동물을 조회합니다.
     * 
     * @param petId 조회할 반려동물의 고유 ID
     * @return 반려동물 엔티티
     * @throws IllegalArgumentException 반려동물을 찾을 수 없는 경우
     */
    public Pet getPetById(Long petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다."));
    }
    
    /**
     * 특정 사용자의 모든 반려동물을 조회합니다.
     * 삭제되지 않은 반려동물만 최신 등록순으로 반환합니다.
     * User 정보를 함께 조회하여 LazyInitializationException을 방지합니다.
     * 
     * @param user 반려동물을 조회할 소유주
     * @return 해당 사용자의 반려동물 목록
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public List<Pet> getPetsByUser(User user) {
        // JOIN FETCH로 User를 함께 로드하여 LazyInitializationException 방지
        // REQUIRED 전파로 기존 트랜잭션이 있으면 참여하고, 없으면 새 트랜잭션 시작
        return petRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDescWithUser(user);
    }
    
    /**
     * 모든 반려동물을 조회합니다.
     * 삭제되지 않은 모든 반려동물을 최신 등록순으로 반환합니다.
     * 관리자가 전체 반려동물 등록 현황을 확인할 때 사용합니다.
     * 
     * @return 모든 반려동물 목록
     */
    public List<Pet> getAllPets() {
        return petRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
    }
    
    /**
     * 특정 동물 종류의 반려동물들을 조회합니다.
     * 개, 고양이 등 특정 종의 반려동물만 필터링해서 볼 수 있습니다.
     * 
     * @param species 조회할 동물 종류
     * @return 해당 종의 반려동물 목록
     */
    public List<Pet> getPetsBySpecies(String species) {
        return petRepository.findBySpeciesAndDeletedAtIsNullOrderByCreatedAtDesc(species);
    }
    
    /**
     * 특정 품종의 반려동물들을 조회합니다.
     * 예를 들어, 골든 리트리버, 페르시안 고양이 등 특정 품종만 필터링합니다.
     * 
     * @param breed 조회할 품종
     * @return 해당 품종의 반려동물 목록
     */
    public List<Pet> getPetsByBreed(String breed) {
        return petRepository.findByBreedAndDeletedAtIsNullOrderByCreatedAtDesc(breed);
    }
    
    @Transactional
    public Pet updatePet(Long petId, String name, String species, String breed, 
                        LocalDate birthDate, String gender, Double weight, 
                        String imageUrl, String description, User user) {
        Pet pet = getPetById(petId);
        
        if (!pet.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("반려동물 정보를 수정할 권한이 없습니다.");
        }
        
        Pet updatedPet = pet.toBuilder()
                .name(name)
                .species(species)
                .breed(breed)
                .birthDate(birthDate)
                .gender(gender)
                .weight(weight)
                .imageUrl(imageUrl)
                .description(description)
                .build();
        
        return petRepository.save(updatedPet);
    }
    
    @Transactional
    public Pet updatePetWeight(Long petId, double weight, User user) {
        Pet pet = getPetById(petId);
        
        if (!pet.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("반려동물 정보를 수정할 권한이 없습니다.");
        }
        
        Pet updatedPet = pet.toBuilder()
                .weight(weight)
                .build();
        
        return petRepository.save(updatedPet);
    }
    
    @Transactional
    public Pet updatePetImage(Long petId, String imageUrl, User user) {
        Pet pet = getPetById(petId);
        
        if (!pet.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("반려동물 정보를 수정할 권한이 없습니다.");
        }
        
        Pet updatedPet = pet.toBuilder()
                .imageUrl(imageUrl)
                .build();
        
        return petRepository.save(updatedPet);
    }
    
    @Transactional
    public void deletePet(Long petId, User user) {
        Pet pet = getPetById(petId);
        
        if (!pet.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("반려동물을 삭제할 권한이 없습니다.");
        }
        
        Pet deletedPet = pet.toBuilder()
                .deletedAt(OffsetDateTime.now())
                .build();
        
        petRepository.save(deletedPet);
    }
    
    public boolean isPetOwner(Long petId, User user) {
        Pet pet = getPetById(petId);
        return pet.getUser().getId().equals(user.getId());
    }
    
    public long getUserPetCount(User user) {
        return petRepository.countByUserAndDeletedAtIsNull(user);
    }
    
    public long getTotalPetCount() {
        return petRepository.countByDeletedAtIsNull();
    }
    
    public long getPetCountBySpecies(String species) {
        return petRepository.countBySpeciesAndDeletedAtIsNull(species);
    }
    
    /**
     * 반려동물의 나이를 계산합니다.
     */
    public int calculateAge(Pet pet) {
        if (pet.getBirthDate() == null) {
            return 0;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate birthDate = pet.getBirthDate();
        
        int age = today.getYear() - birthDate.getYear();
        
        // 생일이 지나지 않았다면 나이에서 1을 빼줍니다.
        if (today.getDayOfYear() < birthDate.getDayOfYear()) {
            age--;
        }
        
        return Math.max(0, age);
    }
    
    /**
     * 반려동물의 BMI 카테고리를 계산합니다 (개의 경우).
     */
    public String calculateBMICategory(Pet pet) {
        if (pet.getWeight() <= 0) {
            return "정보 없음";
        }
        
        // 간단한 체중 기준 (실제로는 견종별로 다르지만 기본 가이드라인)
        double weight = pet.getWeight();
        
        if (weight < 2) {
            return "저체중";
        } else if (weight <= 10) {
            return "소형견";
        } else if (weight <= 25) {
            return "중형견";
        } else {
            return "대형견";
        }
    }
    
    /**
     * 사용자의 주요 반려동물(첫 번째 등록한 반려동물) 조회
     */
    public Pet getPrimaryPet(User user) {
        List<Pet> pets = getPetsByUser(user);
        return pets.isEmpty() ? null : pets.get(0);
    }
}
