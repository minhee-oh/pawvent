package com.pawvent.pawventserver.config;

import com.pawvent.pawventserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JpaConfig {

    private final UserRepository userRepository;

    @Bean
    public ApplicationRunner schemaInitializer() {
        return args -> {
            try {
                log.info("=== 테이블 생성 확인을 위한 초기화 시작 ===");
                // Repository를 호출하여 Hibernate가 엔티티를 초기화하도록 강제
                userRepository.count();
                log.info("=== 초기화 완료 - 테이블이 생성되었습니다 ===");
            } catch (Exception e) {
                log.error("초기화 중 오류 발생", e);
            }
        };
    }
}

