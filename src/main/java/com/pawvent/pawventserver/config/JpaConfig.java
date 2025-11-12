package com.pawvent.pawventserver.config;

import com.pawvent.pawventserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JpaConfig {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public ApplicationRunner schemaInitializer() {
        return args -> {
            try {
                log.info("=== 테이블 생성 확인을 위한 초기화 시작 ===");
                // Repository를 호출하여 Hibernate가 엔티티를 초기화하도록 강제
                userRepository.count();
                
                // category CHECK 제약조건 수정
                try {
                    log.info("=== category CHECK 제약조건 수정 시작 ===");
                    // 기존 제약조건 삭제
                    jdbcTemplate.execute("ALTER TABLE community_post DROP CONSTRAINT IF EXISTS community_post_category_check");
                    // 새로운 제약조건 추가
                    jdbcTemplate.execute("ALTER TABLE community_post ADD CONSTRAINT community_post_category_check CHECK (category IN ('WALK_CERTIFICATION', 'FREE', 'SAFETY'))");
                    log.info("=== category CHECK 제약조건 수정 완료 ===");
                } catch (Exception e) {
                    // 제약조건이 이미 존재하거나 다른 오류가 발생할 수 있음
                    log.warn("category CHECK 제약조건 수정 중 오류 발생 (무시 가능): {}", e.getMessage());
                }
                
                log.info("=== 초기화 완료 - 테이블이 생성되었습니다 ===");
            } catch (Exception e) {
                log.error("초기화 중 오류 발생", e);
            }
        };
    }
}

