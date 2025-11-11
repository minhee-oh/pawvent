package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.service.FileService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 컨트롤러
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    
    private final FileService fileService;
    
    // @PostConstruct를 사용하여 의존성 주입 후 로그 출력
    @PostConstruct
    public void init() {
        log.info("FileController 빈이 생성되었습니다.");
    }
    
    /**
     * 파일 업로드 테스트 엔드포인트
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        log.info("FileController 테스트 엔드포인트 호출됨");
        return ResponseEntity.ok(
            ApiResponse.success("FileController가 정상적으로 작동합니다.", "OK")
        );
    }
    
    /**
     * 반려동물 프로필 이미지 업로드
     */
    @PostMapping("/pets/image")
    public ResponseEntity<ApiResponse<String>> uploadPetImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        log.info("=== FileController.uploadPetImage 메서드 호출됨 ===");
        log.info("인증 정보: {}", authentication != null ? authentication.getName() : "null");
        log.info("파일명: {}, 크기: {}, Content-Type: {}", 
                file != null ? file.getOriginalFilename() : "null",
                file != null ? file.getSize() : 0,
                file != null ? file.getContentType() : "null");
        
        if (file == null) {
            log.warn("파일이 null입니다.");
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("파일이 없습니다."));
        }
        
        if (file.isEmpty()) {
            log.warn("파일이 비어있습니다.");
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("파일이 비어있습니다."));
        }
        
        try {
            log.info("파일 업로드 시작: {}", file.getOriginalFilename());
            String imageUrl = fileService.uploadFile(file, "pets");
            log.info("파일 업로드 성공: {}", imageUrl);
            return ResponseEntity.ok(
                ApiResponse.success("이미지가 업로드되었습니다.", imageUrl)
            );
        } catch (IllegalArgumentException e) {
            log.warn("파일 업로드 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 사용자 프로필 이미지 업로드
     */
    @PostMapping("/users/image")
    public ResponseEntity<ApiResponse<String>> uploadUserImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("파일이 없습니다."));
            }
            
            String imageUrl = fileService.uploadFile(file, "users");
            return ResponseEntity.ok(
                ApiResponse.success("이미지가 업로드되었습니다.", imageUrl)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("이미지 업로드 중 오류가 발생했습니다."));
        }
    }
}

