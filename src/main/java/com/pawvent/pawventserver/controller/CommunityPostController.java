package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.dto.*;
import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.PostCategory;
import com.pawvent.pawventserver.service.CommunityPostService;
import com.pawvent.pawventserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 커뮤니티 게시글 컨트롤러
 * 
 * 커뮤니티 게시글의 생성, 조회, 수정, 삭제 등의 기능을 제공합니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommunityPostController {
    
    private final CommunityPostService communityPostService;
    private final UserService userService;
    
    /**
     * 게시글 작성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        
        CommunityPost post = communityPostService.createPost(
            currentUser,
            request.getTitle(),
            request.getContent(),
            request.getCategory()
        );
        
        PostResponse postResponse = mapToPostResponse(post);
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글이 작성되었습니다.", postResponse)
        );
    }
    
    /**
     * 모든 게시글 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllPosts(Pageable pageable) {
        Page<CommunityPost> posts = communityPostService.getAllPosts(pageable);
        Page<PostResponse> postResponses = posts.map(this::mapToPostResponse);
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글 목록을 조회했습니다.", postResponses)
        );
    }
    
    /**
     * 특정 게시글 조회 (조회수 증가)
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long postId) {
        CommunityPost post = communityPostService.getPostByIdAndIncrementView(postId);
        PostResponse postResponse = mapToPostResponse(post);
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글을 조회했습니다.", postResponse)
        );
    }
    
    /**
     * 카테고리별 게시글 조회
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPostsByCategory(
            @PathVariable PostCategory category,
            Pageable pageable) {
        
        Page<CommunityPost> posts = communityPostService.getPostsByCategory(category, pageable);
        Page<PostResponse> postResponses = posts.map(this::mapToPostResponse);
        
        return ResponseEntity.ok(
            ApiResponse.success(category + " 카테고리 게시글을 조회했습니다.", postResponses)
        );
    }
    
    /**
     * 게시글 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> searchPosts(
            @RequestParam String keyword,
            Pageable pageable) {
        
        Page<CommunityPost> posts = communityPostService.searchPosts(keyword, pageable);
        Page<PostResponse> postResponses = posts.map(this::mapToPostResponse);
        
        return ResponseEntity.ok(
            ApiResponse.success("'" + keyword + "' 검색 결과입니다.", postResponses)
        );
    }
    
    /**
     * 인기 게시글 조회
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPopularPosts(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<CommunityPost> posts = communityPostService.getPopularPosts(limit);
        List<PostResponse> postResponses = posts.stream()
                .map(this::mapToPostResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("인기 게시글을 조회했습니다.", postResponses)
        );
    }
    
    /**
     * 최신 게시글 조회
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getRecentPosts(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<CommunityPost> posts = communityPostService.getRecentPosts(limit);
        List<PostResponse> postResponses = posts.stream()
                .map(this::mapToPostResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("최신 게시글을 조회했습니다.", postResponses)
        );
    }
    
    /**
     * 내가 작성한 게시글 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMyPosts(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        List<CommunityPost> posts = communityPostService.getPostsByUser(currentUser);
        List<PostResponse> postResponses = posts.stream()
                .map(this::mapToPostResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("내가 작성한 게시글을 조회했습니다.", postResponses)
        );
    }
    
    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        
        CommunityPost updatedPost = communityPostService.updatePost(
            postId,
            request.getTitle(),
            request.getContent(),
            request.getCategory(),
            currentUser
        );
        
        PostResponse postResponse = mapToPostResponse(updatedPost);
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글이 수정되었습니다.", postResponse)
        );
    }
    
    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        communityPostService.deletePost(postId, currentUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글이 삭제되었습니다.", null)
        );
    }
    
    /**
     * 게시글 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<PostStats>> getPostStats() {
        long totalPosts = communityPostService.getTotalPostCount();
        
        PostStats stats = PostStats.builder()
                .totalPosts(totalPosts)
                .build();
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글 통계를 조회했습니다.", stats)
        );
    }
    
    /**
     * CommunityPost 엔티티를 PostResponse DTO로 변환
     */
    private PostResponse mapToPostResponse(CommunityPost post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikesCount())
                .authorId(post.getUser().getId())
                .authorNickname(post.getUser().getNickname())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
    
    /**
     * 게시글 통계 내부 클래스
     */
    public static class PostStats {
        private final Long totalPosts;
        
        private PostStats(Long totalPosts) {
            this.totalPosts = totalPosts;
        }
        
        public static PostStatsBuilder builder() {
            return new PostStatsBuilder();
        }
        
        public Long getTotalPosts() {
            return totalPosts;
        }
        
        public static class PostStatsBuilder {
            private Long totalPosts;
            
            public PostStatsBuilder totalPosts(Long totalPosts) {
                this.totalPosts = totalPosts;
                return this;
            }
            
            public PostStats build() {
                return new PostStats(totalPosts);
            }
        }
    }
}


