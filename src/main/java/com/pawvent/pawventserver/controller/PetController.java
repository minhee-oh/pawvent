package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.dto.PetCreateRequest;
import com.pawvent.pawventserver.dto.PetResponse;
import com.pawvent.pawventserver.dto.PetUpdateRequest;
import com.pawvent.pawventserver.domain.Pet;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.service.PetService;
import com.pawvent.pawventserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 반려동물 관리 컨트롤러
 * 
 * 반려동물 등록, 조회, 수정, 삭제 등의 기능을 제공합니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {
    
    private final PetService petService;
    private final UserService userService;
    
    /**
     * 새로운 반려동물을 등록합니다.
     * 사용자는 여러 마리의 반려동물을 등록할 수 있습니다.
     * 
     * @param request 반려동물 등록 정보 (이름, 종, 품종, 생년월일, 성별, 체중 등)
     * @param authentication 현재 인증된 사용자
     * @return 등록된 반려동물 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PetResponse>> createPet(
            @Valid @RequestBody PetCreateRequest request,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        
        Pet pet = petService.createPet(
            currentUser,
            request.getName(),
            request.getSpecies(),
            request.getBreed(),
            request.getBirthDate(),
            request.getGender(),
            request.getWeight(),
            request.getImageUrl(),
            request.getDescription()
        );
        
        PetResponse petResponse = mapToPetResponse(pet);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물이 등록되었습니다.", petResponse)
        );
    }
    
    /**
     * 현재 로그인한 사용자의 모든 반려동물을 조회합니다.
     * 삭제되지 않은 반려동물만 최신 등록순으로 반환됩니다.
     * 
     * @param authentication 현재 인증된 사용자
     * @return 내 반려동물 목록
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getMyPets(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        List<Pet> pets = petService.getPetsByUser(currentUser);
        List<PetResponse> petResponses = pets.stream()
                .map(this::mapToPetResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("내 반려동물 목록을 조회했습니다.", petResponses)
        );
    }
    
    /**
     * ID로 특정 반려동물의 상세 정보를 조회합니다.
     * 모든 사용자가 다른 사용자의 반려동물 정보를 볼 수 있습니다.
     * 
     * @param petId 조회할 반려동물의 고유 ID
     * @return 반려동물의 상세 정보
     */
    @GetMapping("/{petId}")
    public ResponseEntity<ApiResponse<PetResponse>> getPet(@PathVariable Long petId) {
        Pet pet = petService.getPetById(petId);
        PetResponse petResponse = mapToPetResponse(pet);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물 정보를 조회했습니다.", petResponse)
        );
    }
    
    /**
     * 반려동물의 전체 정보를 수정합니다.
     * 반려동물의 주인만 수정 가능하며, 모든 필드를 업데이트합니다.
     * 
     * @param petId 수정할 반려동물의 ID
     * @param request 수정할 반려동물 정보
     * @param authentication 현재 인증된 사용자 (권한 검증용)
     * @return 수정된 반려동물 정보
     */
    @PutMapping("/{petId}")
    public ResponseEntity<ApiResponse<PetResponse>> updatePet(
            @PathVariable Long petId,
            @Valid @RequestBody PetUpdateRequest request,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        
        Pet updatedPet = petService.updatePet(
            petId,
            request.getName(),
            request.getSpecies(),
            request.getBreed(),
            request.getBirthDate(),
            request.getGender(),
            request.getWeight(),
            request.getImageUrl(),
            request.getDescription(),
            currentUser
        );
        
        PetResponse petResponse = mapToPetResponse(updatedPet);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물 정보가 수정되었습니다.", petResponse)
        );
    }
    
    /**
     * 반려동물 체중 업데이트
     */
    @PatchMapping("/{petId}/weight")
    public ResponseEntity<ApiResponse<PetResponse>> updatePetWeight(
            @PathVariable Long petId,
            @RequestParam Double weight,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Pet updatedPet = petService.updatePetWeight(petId, weight, currentUser);
        PetResponse petResponse = mapToPetResponse(updatedPet);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물 체중이 업데이트되었습니다.", petResponse)
        );
    }
    
    /**
     * 반려동물 프로필 이미지 업데이트
     */
    @PatchMapping("/{petId}/image")
    public ResponseEntity<ApiResponse<PetResponse>> updatePetImage(
            @PathVariable Long petId,
            @RequestParam String imageUrl,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Pet updatedPet = petService.updatePetImage(petId, imageUrl, currentUser);
        PetResponse petResponse = mapToPetResponse(updatedPet);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물 프로필 이미지가 업데이트되었습니다.", petResponse)
        );
    }
    
    /**
     * 반려동물 삭제
     */
    @DeleteMapping("/{petId}")
    public ResponseEntity<ApiResponse<Void>> deletePet(
            @PathVariable Long petId,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        petService.deletePet(petId, currentUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물 정보가 삭제되었습니다.", null)
        );
    }
    
    /**
     * 종별 반려동물 조회
     */
    @GetMapping("/species/{species}")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPetsBySpecies(@PathVariable String species) {
        List<Pet> pets = petService.getPetsBySpecies(species);
        List<PetResponse> petResponses = pets.stream()
                .map(this::mapToPetResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success(species + " 종의 반려동물 목록을 조회했습니다.", petResponses)
        );
    }
    
    /**
     * 품종별 반려동물 조회
     */
    @GetMapping("/breeds/{breed}")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPetsByBreed(@PathVariable String breed) {
        List<Pet> pets = petService.getPetsByBreed(breed);
        List<PetResponse> petResponses = pets.stream()
                .map(this::mapToPetResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success(breed + " 품종의 반려동물 목록을 조회했습니다.", petResponses)
        );
    }
    
    /**
     * 반려동물 나이 계산
     */
    @GetMapping("/{petId}/age")
    public ResponseEntity<ApiResponse<Integer>> getPetAge(@PathVariable Long petId) {
        Pet pet = petService.getPetById(petId);
        int age = petService.calculateAge(pet);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물 나이를 계산했습니다.", age)
        );
    }
    
    /**
     * Pet 엔티티를 PetResponse DTO로 변환
     */
    private PetResponse mapToPetResponse(Pet pet) {
        return PetResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .birthDate(pet.getBirthDate())
                .gender(pet.getGender())
                .weight(pet.getWeight())
                .imageUrl(pet.getImageUrl())
                .description(pet.getDescription())
                .ownerId(pet.getUser().getId())
                .ownerNickname(pet.getUser().getNickname())
                .createdAt(pet.getCreatedAt())
                .build();
    }
}
