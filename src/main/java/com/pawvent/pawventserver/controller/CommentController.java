package com.pawvent.pawventserver.controller;

import com.pawvent.pawventserver.dto.ApiResponse;
import com.pawvent.pawventserver.dto.CommentCreateRequest;
import com.pawvent.pawventserver.dto.CommentResponse;
import com.pawvent.pawventserver.domain.Comment;
import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.service.CommentService;
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
 * 댓글 관리 컨트롤러
 * 
 * 게시글 댓글과 대댓글의 생성, 조회, 수정, 삭제 등의 기능을 제공합니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    private final CommunityPostService communityPostService;
    private final UserService userService;
    
    /**
     * 커뮤니티 게시글에 새로운 댓글을 작성합니다.
     * 인증된 사용자만 댓글을 작성할 수 있으며, 게시글 ID와 댓글 내용을 필요로 합니다.
     * 
     * @param request 댓글 생성 요청 데이터 (게시글 ID, 내용)
     * @param authentication 현재 인증된 사용자
     * @return 생성된 댓글 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Valid @RequestBody CommentCreateRequest request,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        CommunityPost post = communityPostService.getPostById(request.getPostId());
        
        Comment comment = commentService.createComment(currentUser, post, request.getContent());
        CommentResponse commentResponse = mapToCommentResponse(comment);
        
        return ResponseEntity.ok(
            ApiResponse.success("댓글이 작성되었습니다.", commentResponse)
        );
    }
    
    /**
     * 특정 게시글의 댓글 조회
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByPost(@PathVariable Long postId) {
        CommunityPost post = communityPostService.getPostById(postId);
        List<Comment> comments = commentService.getCommentsByPost(post);
        List<CommentResponse> commentResponses = comments.stream()
                .map(this::mapToCommentResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("게시글의 댓글을 조회했습니다.", commentResponses)
        );
    }
    
    /**
     * 내가 작성한 댓글 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getMyComments(Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        List<Comment> comments = commentService.getCommentsByUser(currentUser);
        List<CommentResponse> commentResponses = comments.stream()
                .map(this::mapToCommentResponse)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("내가 작성한 댓글을 조회했습니다.", commentResponses)
        );
    }
    
    /**
     * 특정 댓글 조회
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(@PathVariable Long commentId) {
        Comment comment = commentService.getCommentById(commentId);
        CommentResponse commentResponse = mapToCommentResponse(comment);
        
        return ResponseEntity.ok(
            ApiResponse.success("댓글을 조회했습니다.", commentResponse)
        );
    }
    
    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @RequestParam String content,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        Comment updatedComment = commentService.updateComment(commentId, content, currentUser);
        CommentResponse commentResponse = mapToCommentResponse(updatedComment);
        
        return ResponseEntity.ok(
            ApiResponse.success("댓글이 수정되었습니다.", commentResponse)
        );
    }
    
    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        
        User currentUser = userService.getCurrentUser(authentication);
        commentService.deleteComment(commentId, currentUser);
        
        return ResponseEntity.ok(
            ApiResponse.success("댓글이 삭제되었습니다.", null)
        );
    }
    
    /**
     * 특정 게시글의 댓글 수 조회
     */
    @GetMapping("/post/{postId}/count")
    public ResponseEntity<ApiResponse<Long>> getCommentCount(@PathVariable Long postId) {
        CommunityPost post = communityPostService.getPostById(postId);
        long commentCount = commentService.getCommentCount(post);
        
        return ResponseEntity.ok(
            ApiResponse.success("댓글 수를 조회했습니다.", commentCount)
        );
    }
    
    /**
     * Comment 엔티티를 CommentResponse DTO로 변환
     */
    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getUser().getId())
                .authorNickname(comment.getUser().getNickname())
                .postId(comment.getPost().getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
