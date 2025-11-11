package com.pawvent.pawventserver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 파일 업로드 및 관리 서비스
 */
@Service
@Slf4j
public class FileService {
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    @Value("${server.port:8081}")
    private int serverPort;
    
    // @PostConstruct를 사용하여 의존성 주입 후 로그 출력
    @PostConstruct
    public void init() {
        log.info("FileService 빈이 생성되었습니다. uploadDir={}, serverPort={}", uploadDir, serverPort);
    }
    
    /**
     * 파일을 업로드하고 접근 가능한 URL을 반환합니다.
     * 
     * @param file 업로드할 파일
     * @param subDirectory 서브 디렉토리 (예: "pets", "users")
     * @return 파일 접근 URL
     * @throws IOException 파일 저장 실패 시
     */
    public String uploadFile(MultipartFile file, String subDirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("파일 이름이 없습니다.");
        }
        
        String extension = getFileExtension(originalFilename);
        if (!isValidImageExtension(extension)) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다. (jpg, jpeg, png, gif, webp)");
        }
        
        // 파일 크기 검증 (10MB 제한)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
        
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 고유한 파일명 생성
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        // 파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("파일 업로드 성공: {}", filePath);
        
        // 접근 가능한 URL 반환
        return String.format("http://localhost:%d/uploads/%s/%s", serverPort, subDirectory, uniqueFilename);
    }
    
    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex).toLowerCase();
    }
    
    /**
     * 이미지 파일 확장자 검증
     */
    private boolean isValidImageExtension(String extension) {
        return extension.equals(".jpg") || 
               extension.equals(".jpeg") || 
               extension.equals(".png") || 
               extension.equals(".gif") || 
               extension.equals(".webp");
    }
    
    /**
     * 파일 삭제
     */
    public void deleteFile(String fileUrl) {
        try {
            // URL에서 파일 경로 추출
            // 예: http://localhost:8081/uploads/pets/uuid.jpg -> uploads/pets/uuid.jpg
            String path = fileUrl.substring(fileUrl.indexOf("/uploads/") + 1);
            Path filePath = Paths.get(path);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("파일 삭제 성공: {}", filePath);
            }
        } catch (Exception e) {
            log.warn("파일 삭제 실패: {}", fileUrl, e);
        }
    }
}

