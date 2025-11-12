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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public ResponseEntity<ApiResponse<PetResponse>> createPet(
            @Valid @RequestBody PetCreateRequest request,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        
        // 트랜잭션 내에서 User의 nickname을 미리 초기화
        String userNickname = currentUser.getNickname();
        
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
        
        // userNickname을 직접 전달하여 user.getNickname() 호출을 피함
        PetResponse petResponse = mapToPetResponse(pet, currentUser, userNickname);
        
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
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<PetResponse>>> getMyPets(Authentication authentication) {
        // 모든 작업을 하나의 트랜잭션 내에서 수행
        // 1. User 조회 및 nickname 초기화
        User currentUser = userService.getCurrentUser(authentication);
        Long userId = currentUser.getId();
        User managedUser = userService.getUserById(userId);
        String userNickname = managedUser.getNickname(); // 트랜잭션 내에서 초기화
        
        // 2. JOIN FETCH로 Pet과 User를 함께 로드
        List<Pet> pets = petService.getPetsByUser(managedUser);
        
        // 3. 트랜잭션 내에서 DTO 변환 (트랜잭션이 종료되기 전에 완료)
        final String finalNickname = userNickname;
        List<PetResponse> petResponses = new java.util.ArrayList<>(pets.size());
        for (Pet pet : pets) {
            // userNickname을 직접 전달하여 user.getNickname() 호출 방지
            petResponses.add(mapToPetResponse(pet, managedUser, finalNickname));
        }
        
        return ResponseEntity.ok(
            ApiResponse.success("내 반려동물 목록을 조회했습니다.", petResponses)
        );
    }
    
    /**
     * 특정 사용자의 모든 반려동물을 조회합니다.
     * 삭제되지 않은 반려동물만 최신 등록순으로 반환됩니다.
     * 모든 사용자가 다른 사용자의 반려동물 정보를 볼 수 있습니다.
     * 
     * @param userId 조회할 사용자의 고유 ID
     * @return 해당 사용자의 반려동물 목록
     */
    @GetMapping("/by-user/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPetsByUserId(@PathVariable("userId") Long userId) {
        User user = userService.getUserById(userId);
        String userNickname = user.getNickname();
        
        List<Pet> pets = petService.getPetsByUser(user);
        final String finalNickname = userNickname;
        List<PetResponse> petResponses = new java.util.ArrayList<>(pets.size());
        for (Pet pet : pets) {
            petResponses.add(mapToPetResponse(pet, user, finalNickname));
        }
        
        return ResponseEntity.ok(
            ApiResponse.success("사용자의 반려동물 목록을 조회했습니다.", petResponses)
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
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PetResponse>> getPet(@PathVariable("petId") Long petId) {
        // JOIN FETCH로 User를 함께 로드
        Pet pet = petService.getPetByIdWithUser(petId);
        User user = pet.getUser();
        
        // 트랜잭션 내에서 User의 nickname을 미리 초기화
        // JOIN FETCH로 로드되었지만, 명시적으로 접근하여 초기화 보장
        String userNickname = (user != null && user.getNickname() != null) 
                ? user.getNickname() 
                : "사용자";
        
        PetResponse petResponse = mapToPetResponse(pet, user, userNickname);
        
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
    @Transactional
    public ResponseEntity<ApiResponse<PetResponse>> updatePet(
            @PathVariable("petId") Long petId,
            @Valid @RequestBody PetUpdateRequest request,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Long userId = currentUser.getId();
        User managedUser = userService.getUserById(userId);
        String userNickname = managedUser.getNickname();
        
        // name이 null이면 기존 이름 유지, null이 아니면 업데이트
        String name = (request.getName() != null && !request.getName().trim().isEmpty()) 
                ? request.getName().trim() 
                : null;
        
        Pet updatedPet = petService.updatePet(
            petId,
            name,
            request.getSpecies(),
            request.getBreed(),
            request.getBirthDate(),
            request.getGender(),
            request.getWeight(),
            request.getImageUrl(),
            request.getDescription(),
            managedUser
        );
        
        // 트랜잭션 내에서 DTO 변환
        PetResponse petResponse = mapToPetResponse(updatedPet, managedUser, userNickname);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물 정보가 수정되었습니다.", petResponse)
        );
    }
    
    /**
     * 반려동물 체중 업데이트
     */
    @PatchMapping("/{petId}/weight")
    @Transactional
    public ResponseEntity<ApiResponse<PetResponse>> updatePetWeight(
            @PathVariable("petId") Long petId,
            @RequestParam Double weight,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Long userId = currentUser.getId();
        User managedUser = userService.getUserById(userId);
        String userNickname = managedUser.getNickname();
        
        Pet updatedPet = petService.updatePetWeight(petId, weight, managedUser);
        PetResponse petResponse = mapToPetResponse(updatedPet, managedUser, userNickname);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물 체중이 업데이트되었습니다.", petResponse)
        );
    }
    
    /**
     * 반려동물 프로필 이미지 업데이트
     */
    @PatchMapping("/{petId}/image")
    @Transactional
    public ResponseEntity<ApiResponse<PetResponse>> updatePetImage(
            @PathVariable("petId") Long petId,
            @RequestParam String imageUrl,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Long userId = currentUser.getId();
        User managedUser = userService.getUserById(userId);
        String userNickname = managedUser.getNickname();
        
        Pet updatedPet = petService.updatePetImage(petId, imageUrl, managedUser);
        PetResponse petResponse = mapToPetResponse(updatedPet, managedUser, userNickname);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물 프로필 이미지가 업데이트되었습니다.", petResponse)
        );
    }
    
    /**
     * 반려동물 삭제
     */
    @DeleteMapping("/{petId}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deletePet(
            @PathVariable("petId") Long petId,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Long userId = currentUser.getId();
        User managedUser = userService.getUserById(userId);
        petService.deletePet(petId, managedUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물 정보가 삭제되었습니다.", null)
        );
    }
    
    /**
     * 종별 반려동물 조회
     */
    @GetMapping("/species/{species}")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPetsBySpecies(@PathVariable("species") String species) {
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
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPetsByBreed(@PathVariable("breed") String breed) {
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
    public ResponseEntity<ApiResponse<Integer>> getPetAge(@PathVariable("petId") Long petId) {
        Pet pet = petService.getPetById(petId);
        int age = petService.calculateAge(pet);
        
        return ResponseEntity.ok(
            ApiResponse.success("반려동물 나이를 계산했습니다.", age)
        );
    }
    
    /**
     * Pet 엔티티를 PetResponse DTO로 변환
     * 주의: 이 메서드는 트랜잭션 내에서 호출되어야 하며, userNickname을 반드시 전달해야 합니다.
     * 이 메서드는 사용하지 않는 것이 좋습니다. 대신 mapToPetResponse(Pet pet, User user, String userNickname)를 사용하세요.
     */
    private PetResponse mapToPetResponse(Pet pet) {
        // User를 안전하게 가져오기 위해 트랜잭션 내에서 호출되어야 함
        // 하지만 userNickname을 알 수 없으므로 기본값 사용
        User user = pet.getUser();
        return mapToPetResponse(pet, user, null);
    }
    
    /**
     * Pet 엔티티를 PetResponse DTO로 변환 (User 정보를 직접 전달)
     * 주의: 이 메서드는 트랜잭션 내에서 호출되어야 하며, user는 managed 상태여야 합니다.
     * 가능하면 mapToPetResponse(Pet pet, User user, String userNickname)를 사용하세요.
     */
    private PetResponse mapToPetResponse(Pet pet, User user) {
        // user.getNickname()을 호출하지 않고, null을 전달하여 안전하게 처리
        // 호출하는 쪽에서 userNickname을 미리 가져와서 전달해야 함
        return mapToPetResponse(pet, user, null);
    }
    
    /**
     * Pet 엔티티를 PetResponse DTO로 변환 (User와 nickname을 직접 전달)
     * 이 메서드는 user.getNickname()을 절대 호출하지 않습니다.
     */
    private PetResponse mapToPetResponse(Pet pet, User user, String userNickname) {
        // userNickname이 null이면 기본값 사용 (user.getNickname()을 절대 호출하지 않음)
        String nickname = (userNickname != null && !userNickname.isEmpty()) ? userNickname : "사용자";
        
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
                .ownerId(user.getId())
                .ownerNickname(nickname)
                .createdAt(pet.getCreatedAt())
                .build();
    }
}
