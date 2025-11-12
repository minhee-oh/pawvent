package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.enums.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user u WHERE p.id = :id AND p.deletedAt IS NULL")
    java.util.Optional<CommunityPost> findByIdWithUser(@Param("id") Long id);

    @Query("""
        SELECT p 
        FROM CommunityPost p 
        WHERE p.deletedAt IS NULL 
          AND (:category IS NULL OR p.category = :category)
        ORDER BY p.createdAt DESC
    """)
    Page<CommunityPost> findByCategoryNullable(
            @Param("category") PostCategory category,
            Pageable pageable
    );
}
