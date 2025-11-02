package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 커뮤니티 게시글 관련 데이터베이스 접근을 담당하는 레포지토리
 * 게시글 조회, 저장, 수정, 검색 기능을 제공
 */
@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    
    /**
     * 모든 게시글을 최신순으로 페이징 조회
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Query("SELECT c FROM CommunityPost c ORDER BY c.createdAt DESC")
    Page<CommunityPost> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 특정 사용자의 게시글을 최신순으로 조회
     * @param user 작성자
     * @return 해당 사용자의 게시글 목록
     */
    @Query("SELECT c FROM CommunityPost c WHERE c.user = :user ORDER BY c.createdAt DESC")
    List<CommunityPost> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    /**
     * 카테고리별 게시글을 최신순으로 페이징 조회
     * @param category 게시글 카테고리
     * @param pageable 페이징 정보
     * @return 해당 카테고리의 게시글 페이지
     */
    @Query("SELECT c FROM CommunityPost c WHERE c.category = :category ORDER BY c.createdAt DESC")
    Page<CommunityPost> findByCategoryOrderByCreatedAtDesc(@Param("category") PostCategory category, Pageable pageable);
    
    /**
     * 제목으로 게시글 검색 (부분 일치, 대소문자 무시)
     * @param title 검색할 제목
     * @param pageable 페이징 정보
     * @return 검색된 게시글 페이지
     */
    @Query("SELECT c FROM CommunityPost c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY c.createdAt DESC")
    Page<CommunityPost> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);
    
    /**
     * 내용으로 게시글 검색 (부분 일치, 대소문자 무시)
     * @param content 검색할 내용
     * @param pageable 페이징 정보
     * @return 검색된 게시글 페이지
     */
    @Query("SELECT c FROM CommunityPost c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :content, '%')) ORDER BY c.createdAt DESC")
    Page<CommunityPost> findByContentContainingIgnoreCase(@Param("content") String content, Pageable pageable);
    
    /**
     * 제목 또는 내용으로 게시글 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 게시글 페이지
     */
    @Query("SELECT c FROM CommunityPost c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY c.createdAt DESC")
    Page<CommunityPost> findByTitleOrContentContaining(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 좋아요 수가 많은 게시글 순으로 조회 (인기 게시글)
     * @param pageable 페이징 정보
     * @return 인기 게시글 페이지
     */
    @Query("SELECT c FROM CommunityPost c ORDER BY c.likesCount DESC, c.createdAt DESC")
    Page<CommunityPost> findPopularPosts(Pageable pageable);
    
    /**
     * 댓글 수가 많은 게시글 순으로 조회 (활발한 토론 게시글)
     * @param pageable 페이징 정보
     * @return 댓글이 많은 게시글 페이지
     */
    @Query("SELECT c FROM CommunityPost c ORDER BY c.commentsCount DESC, c.createdAt DESC")
    Page<CommunityPost> findActiveDiscussionPosts(Pageable pageable);
    
    /**
     * 특정 카테고리에서 인기 게시글 조회
     * @param category 게시글 카테고리
     * @param pageable 페이징 정보
     * @return 해당 카테고리의 인기 게시글
     */
    @Query("SELECT c FROM CommunityPost c WHERE c.category = :category " +
           "ORDER BY c.likesCount DESC, c.createdAt DESC")
    Page<CommunityPost> findPopularPostsByCategory(@Param("category") PostCategory category, Pageable pageable);
    
    /**
     * 특정 사용자의 게시글 수 조회
     * @param user 작성자
     * @return 해당 사용자의 총 게시글 수
     */
    long countByUser(User user);
    
    /**
     * 삭제되지 않은 게시글만 조회 (소프트 삭제 고려)
     * @param pageable 페이징 정보
     * @return 활성 게시글 페이지
     */
    @Query("SELECT c FROM CommunityPost c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    Page<CommunityPost> findActivePostsOrderByCreatedAtDesc(Pageable pageable);
    
    // 추가 필요한 메서드들
    Page<CommunityPost> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
    Page<CommunityPost> findByCategoryAndDeletedAtIsNullOrderByCreatedAtDesc(PostCategory category, Pageable pageable);
    List<CommunityPost> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(User user);
    Page<CommunityPost> findByTitleContainingOrContentContainingAndDeletedAtIsNullOrderByCreatedAtDesc(
            String title, String content, Pageable pageable);
    
    // 인기 게시글 조회 메서드들
    @Query(value = "SELECT * FROM community_post WHERE deleted_at IS NULL ORDER BY likes_count DESC, view_count DESC LIMIT :limit", 
           nativeQuery = true)
    List<CommunityPost> findTopByOrderByLikeCountDescViewCountDesc(@Param("limit") int limit);
    
    @Query(value = "SELECT * FROM community_post WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT :limit", 
           nativeQuery = true)
    List<CommunityPost> findTopByOrderByCreatedAtDesc(@Param("limit") int limit);
    
    // 카운트 메서드들
    long countByDeletedAtIsNull();
    long countByCategoryAndDeletedAtIsNull(PostCategory category);
    long countByUserAndDeletedAtIsNull(User user);
}
