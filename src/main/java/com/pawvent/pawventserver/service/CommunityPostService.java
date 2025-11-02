package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.PostCategory;
import com.pawvent.pawventserver.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostService {
    
    private final CommunityPostRepository communityPostRepository;
    private final CommentService commentService;
    
    /**
     * 새로운 커뮤니티 게시글을 생성합니다.
     * 
     * @param user 게시글 작성자
     * @param title 게시글 제목
     * @param content 게시글 내용
     * @param category 게시글 카테고리
     * @param imageUrls 첨부 이미지 URL 목록
     * @return 생성된 게시글 엔티티
     */
    @Transactional
    public CommunityPost createPost(User user, String title, String content, PostCategory category) {
        CommunityPost post = CommunityPost.builder()
                .user(user)
                .title(title)
                .content(content)
                .category(category)
                .viewCount(0)
                .likesCount(0)
                .build();
        
        return communityPostRepository.save(post);
    }
    
    /**
     * 게시글 ID로 특정 게시글을 조회합니다.
     * 
     * @param postId 조회할 게시글의 ID
     * @return 해당 게시글 엔티티
     * @throws IllegalArgumentException 게시글을 찾을 수 없는 경우
     */
    public CommunityPost getPostById(Long postId) {
        return communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }
    
    /**
     * 게시글을 조회하고 조회수를 1 증가시킵니다.
     * 게시글 상세 페이지 접근 시 사용됩니다.
     * 
     * @param postId 조회할 게시글의 ID
     * @return 조회수가 증가된 게시글 엔티티
     */
    @Transactional
    public CommunityPost getPostByIdAndIncrementView(Long postId) {
        CommunityPost post = getPostById(postId);
        
        CommunityPost updatedPost = post.toBuilder()
                .viewCount(post.getViewCount() + 1)
                .build();
        
        return communityPostRepository.save(updatedPost);
    }
    
    /**
     * 모든 게시글을 페이징하여 조회합니다.
     * 삭제되지 않은 게시글만 최신순으로 반환합니다.
     * 
     * @param pageable 페이징 정보 (페이지 번호, 크기, 정렬 옵션)
     * @return 페이징된 게시글 목록
     */
    public Page<CommunityPost> getAllPosts(Pageable pageable) {
        return communityPostRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * 특정 카테고리의 게시글을 페이징하여 조회합니다.
     * 
     * @param category 조회할 게시글 카테고리
     * @param pageable 페이징 정보
     * @return 해당 카테고리의 페이징된 게시글 목록
     */
    public Page<CommunityPost> getPostsByCategory(PostCategory category, Pageable pageable) {
        return communityPostRepository.findByCategoryAndDeletedAtIsNullOrderByCreatedAtDesc(category, pageable);
    }
    
    /**
     * 특정 사용자가 작성한 모든 게시글을 조회합니다.
     * 
     * @param user 게시글 작성자
     * @return 해당 사용자의 게시글 목록 (최신순)
     */
    public List<CommunityPost> getPostsByUser(User user) {
        return communityPostRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user);
    }
    
    /**
     * 키워드로 게시글을 검색합니다.
     * 제목과 내용에서 키워드를 검색하여 일치하는 게시글을 반환합니다.
     * 
     * @param keyword 검색할 키워드
     * @param pageable 페이징 정보
     * @return 검색된 게시글 목록
     */
    public Page<CommunityPost> searchPosts(String keyword, Pageable pageable) {
        return communityPostRepository.findByTitleContainingOrContentContainingAndDeletedAtIsNullOrderByCreatedAtDesc(
                keyword, keyword, pageable);
    }
    
    /**
     * 인기 게시글을 조회합니다.
     * 좋아요 수와 조회수를 기준으로 정렬된 상위 게시글을 반환합니다.
     * 
     * @param limit 조회할 게시글 개수
     * @return 인기 게시글 목록
     */
    public List<CommunityPost> getPopularPosts(int limit) {
        return communityPostRepository.findTopByOrderByLikeCountDescViewCountDesc(limit);
    }
    
    /**
     * 최신 게시글을 조회합니다.
     * 작성일을 기준으로 가장 최근에 작성된 게시글들을 반환합니다.
     * 
     * @param limit 조회할 게시글 개수
     * @return 최신 게시글 목록
     */
    public List<CommunityPost> getRecentPosts(int limit) {
        return communityPostRepository.findTopByOrderByCreatedAtDesc(limit);
    }
    
    /**
     * 게시글을 수정합니다.
     * 작성자만 자신의 게시글을 수정할 수 있습니다.
     * 
     * @param postId 수정할 게시글의 ID
     * @param title 새로운 제목
     * @param content 새로운 내용
     * @param category 새로운 카테고리
     * @param imageUrls 새로운 이미지 URL 목록
     * @param user 수정을 요청한 사용자 (권한 검증용)
     * @return 수정된 게시글 엔티티
     * @throws IllegalArgumentException 수정 권한이 없는 경우
     */
    @Transactional
    public CommunityPost updatePost(Long postId, String title, String content, 
                                  PostCategory category, User user) {
        CommunityPost post = getPostById(postId);
        
        if (!post.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
        }
        
        CommunityPost updatedPost = post.toBuilder()
                .title(title)
                .content(content)
                .category(category)
                .build();
        
        return communityPostRepository.save(updatedPost);
    }
    
    /**
     * 게시글을 삭제합니다 (소프트 삭제).
     * 작성자만 자신의 게시글을 삭제할 수 있으며, 연관된 댓글들도 함께 삭제됩니다.
     * 
     * @param postId 삭제할 게시글의 ID
     * @param user 삭제를 요청한 사용자 (권한 검증용)
     * @throws IllegalArgumentException 삭제 권한이 없는 경우
     */
    @Transactional
    public void deletePost(Long postId, User user) {
        CommunityPost post = getPostById(postId);
        
        if (!post.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다.");
        }
        
        // 연관된 댓글들도 삭제
        commentService.deleteCommentsByPost(post);
        
        CommunityPost deletedPost = post.toBuilder()
                .deletedAt(OffsetDateTime.now())
                .build();
        
        communityPostRepository.save(deletedPost);
    }
    
    /**
     * 게시글의 좋아요 수를 1 증가시킵니다.
     * PostLikeService에서 좋아요 추가 시 호췜됩니다.
     * 
     * @param postId 좋아요를 증가시킬 게시글 ID
     */
    @Transactional
    public void incrementLikeCount(Long postId) {
        CommunityPost post = getPostById(postId);
        
        CommunityPost updatedPost = post.toBuilder()
                .likesCount(post.getLikesCount() + 1)
                .build();
        
        communityPostRepository.save(updatedPost);
    }
    
    /**
     * 게시글의 좋아요 수를 1 감소시킵니다.
     * PostLikeService에서 좋아요 취소 시 호췜됩니다.
     * 좋아요 수가 0 아래로 떨어지지 않도록 보장합니다.
     * 
     * @param postId 좋아요를 감소시킬 게시글 ID
     */
    @Transactional
    public void decrementLikeCount(Long postId) {
        CommunityPost post = getPostById(postId);
        
        int newLikeCount = Math.max(0, post.getLikesCount() - 1);
        CommunityPost updatedPost = post.toBuilder()
                .likesCount(newLikeCount)
                .build();
        
        communityPostRepository.save(updatedPost);
    }
    
    /**
     * 사용자가 해당 게시글의 소유자(작성자)인지 확인합니다.
     * 권한 검증에 사용됩니다.
     * 
     * @param postId 확인할 게시글 ID
     * @param user 확인할 사용자
     * @return 소유자이면 true, 그렇지 않으면 false
     */
    public boolean isPostOwner(Long postId, User user) {
        CommunityPost post = getPostById(postId);
        return post.getUser().getId().equals(user.getId());
    }
    
    /**
     * 전체 게시글 수를 조회합니다.
     * 삭제되지 않은 게시글만 삼합니다.
     * 
     * @return 전체 게시글 수
     */
    public long getTotalPostCount() {
        return communityPostRepository.countByDeletedAtIsNull();
    }
    
    /**
     * 특정 카테고리의 게시글 수를 조회합니다.
     * 
     * @param category 카테고리
     * @return 해당 카테고리의 게시글 수
     */
    public long getPostCountByCategory(PostCategory category) {
        return communityPostRepository.countByCategoryAndDeletedAtIsNull(category);
    }
    
    /**
     * 특정 사용자가 작성한 게시글 수를 조회합니다.
     * 
     * @param user 사용자
     * @return 해당 사용자의 게시글 수
     */
    public long getUserPostCount(User user) {
        return communityPostRepository.countByUserAndDeletedAtIsNull(user);
    }
}
