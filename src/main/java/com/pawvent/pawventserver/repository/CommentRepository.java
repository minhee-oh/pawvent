package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.Comment;
import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 댓글 관련 데이터베이스 접근을 담당하는 레포지토리
 * 게시글 댓글의 조회, 저장, 수정, 삭제 기능을 제공
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 특정 게시글의 모든 댓글 조회 (삭제된 것 포함)
     * @param post 게시글
     * @return 해당 게시글의 모든 댓글 목록
     */
    List<Comment> findByPost(CommunityPost post);
    
    /**
     * 특정 게시글의 활성 댓글만 최신순으로 조회
     * @param post 게시글
     * @return 해당 게시글의 댓글 목록 (최신순, 삭제 제외)
     */
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findByPostOrderByCreatedAtDesc(@Param("post") CommunityPost post);
    
    /**
     * 특정 게시글의 활성 댓글을 페이징으로 조회 (시간순)
     * @param post 게시글
     * @param pageable 페이징 정보
     * @return 해당 게시글의 댓글 페이지
     */
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findByPostWithPaging(@Param("post") CommunityPost post, Pageable pageable);
    
    /**
     * 특정 사용자의 모든 활성 댓글을 최신순으로 조회
     * @param user 작성자
     * @return 해당 사용자의 댓글 목록 (삭제 제외)
     */
    @Query("SELECT c FROM Comment c WHERE c.user = :user AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    /**
     * 특정 게시글의 활성 댓글을 가장 오래된 순으로 조회 (시간순 정렬)
     * @param post 게시글
     * @return 해당 게시글의 댓글 목록 (오래된 순, 삭제 제외)
     */
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findByPostOrderByCreatedAtAsc(@Param("post") CommunityPost post);
    
    /**
     * 특정 게시글의 활성 댓글 수 조회
     * @param post 게시글
     * @return 해당 게시글의 댓글 수 (삭제 제외)
     */
    long countByPostAndDeletedAtIsNull(CommunityPost post);
    
    /**
     * 특정 사용자의 활성 댓글 수 조회
     * @param user 작성자
     * @return 해당 사용자의 총 댓글 수 (삭제 제외)
     */
    long countByUserAndDeletedAtIsNull(User user);
    
    /**
     * 특정 사용자가 특정 게시글에 작성한 활성 댓글들 조회
     * @param user 작성자
     * @param post 게시글
     * @return 해당 사용자가 작성한 댓글 목록 (삭제 제외)
     */
    @Query("SELECT c FROM Comment c WHERE c.user = :user AND c.post = :post AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findByUserAndPost(@Param("user") User user, @Param("post") CommunityPost post);
    
    /**
     * 최근 N개의 활성 댓글 조회
     * @param post 게시글
     * @param pageable 페이징 정보 (limit 용도)
     * @return 최신 댓글 목록 (삭제 제외)
     */
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findLatestCommentsByPost(@Param("post") CommunityPost post, Pageable pageable);
    
    /**
     * 내용으로 활성 댓글 검색
     * @param content 검색할 내용
     * @param pageable 페이징 정보
     * @return 검색된 댓글 페이지 (삭제 제외)
     */
    @Query("SELECT c FROM Comment c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :content, '%')) AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findByContentContaining(@Param("content") String content, Pageable pageable);
}
