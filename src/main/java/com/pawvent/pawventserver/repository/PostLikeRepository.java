package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.PostLike;
import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 게시글 좋아요 관련 데이터베이스 접근을 담당하는 레포지토리
 * 사용자의 게시글 좋아요 상태를 관리하는 기능을 제공
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    /**
     * 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
     * @param user 사용자
     * @param post 게시글
     * @return 좋아요 엔티티 (없으면 Empty)
     */
    Optional<PostLike> findByUserAndPost(User user, CommunityPost post);
    
    /**
     * 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 여부 확인
     * @param user 사용자
     * @param post 게시글
     * @return 좋아요 여부
     */
    boolean existsByUserAndPost(User user, CommunityPost post);
    
    /**
     * 특정 게시글의 모든 좋아요 조회
     * @param post 게시글
     * @return 해당 게시글의 좋아요 목록
     */
    List<PostLike> findByPost(CommunityPost post);
    
    /**
     * 특정 사용자의 모든 좋아요 조회
     * @param user 사용자
     * @return 해당 사용자의 좋아요 목록
     */
    List<PostLike> findByUser(User user);
    
    /**
     * 특정 사용자가 좋아요한 모든 게시글 조회 (최신순)
     * @param user 사용자
     * @return 해당 사용자가 좋아요한 게시글의 좋아요 목록
     */
    @Query("SELECT pl FROM PostLike pl WHERE pl.user = :user ORDER BY pl.createdAt DESC")
    List<PostLike> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    /**
     * 특정 게시글의 좋아요 수 조회
     * @param post 게시글
     * @return 해당 게시글의 총 좋아요 수
     */
    long countByPost(CommunityPost post);
    
    /**
     * 특정 사용자의 총 좋아요 수 조회 (받은 좋아요)
     * @param author 게시글 작성자
     * @return 해당 사용자가 받은 총 좋아요 수
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.user = :author")
    long countReceivedLikesByAuthor(@Param("author") User author);
    
    /**
     * 특정 사용자가 누른 좋아요 수 조회 (준 좋아요)
     * @param user 사용자
     * @return 해당 사용자가 누른 총 좋아요 수
     */
    long countByUser(User user);
    
    /**
     * 특정 게시글의 좋아요를 최신순으로 조회 (좋아요한 사용자 목록)
     * @param post 게시글
     * @return 해당 게시글에 좋아요한 사용자들의 좋아요 목록
     */
    @Query("SELECT pl FROM PostLike pl WHERE pl.post = :post ORDER BY pl.createdAt DESC")
    List<PostLike> findByPostOrderByCreatedAtDesc(@Param("post") CommunityPost post);
    
    /**
     * 사용자와 게시글로 좋아요 삭제
     * @param user 사용자
     * @param post 게시글
     */
    void deleteByUserAndPost(User user, CommunityPost post);
    
    /**
     * 특정 게시글의 모든 좋아요 삭제
     * @param post 게시글
     */
    void deleteByPost(CommunityPost post);
}



