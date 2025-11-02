package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(Long kakaoId);
    Optional<User> findByNickname(String nickname);
    Optional<User> findByEmail(String email);
    
    // 활성 사용자 관련 메서드들
    List<User> findByDeletedAtIsNull();
    Page<User> findByDeletedAtIsNull(Pageable pageable);
    long countByDeletedAtIsNull();
    
    // 검색 관련 메서드들
    List<User> findByNicknameContainingAndDeletedAtIsNull(String nickname);
}
