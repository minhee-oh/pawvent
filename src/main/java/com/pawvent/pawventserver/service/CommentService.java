package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.Comment;
import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.repository.CommentRepository;
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
public class CommentService {
    
    private final CommentRepository commentRepository;
    
    /**
     * 새로운 댓글을 생성합니다.
     * 커뮤니티 게시글에 사용자들이 의견을 남길 수 있습니다.
     * 
     * @param user 댓글을 작성하는 사용자
     * @param post 댓글이 속한 커뮤니티 게시글
     * @param content 댓글 내용
     * @return 생성된 댓글 엔티티
     */
    @Transactional
    public Comment createComment(User user, CommunityPost post, String content) {
        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content(content)
                .build();
        
        return commentRepository.save(comment);
    }
    
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }
    
    public List<Comment> getCommentsByPost(CommunityPost post) {
        return commentRepository.findByPost(post);
    }
    
    public List<Comment> getCommentsByUser(User user) {
        return commentRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    @Transactional
    public Comment updateComment(Long commentId, String content, User user) {
        Comment comment = getCommentById(commentId);
        
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다.");
        }
        
        Comment updatedComment = comment.toBuilder()
                .content(content)
                .build();
        
        return commentRepository.save(updatedComment);
    }
    
    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = getCommentById(commentId);
        
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }
        
        Comment deletedComment = comment.toBuilder()
                .deletedAt(OffsetDateTime.now())
                .build();
        
        commentRepository.save(deletedComment);
    }
    
    @Transactional
    public void deleteCommentsByPost(CommunityPost post) {
        List<Comment> comments = commentRepository.findByPost(post);
        OffsetDateTime now = OffsetDateTime.now();
        
        List<Comment> deletedComments = comments.stream()
                .map(comment -> comment.toBuilder().deletedAt(now).build())
                .toList();
        
        commentRepository.saveAll(deletedComments);
    }
    
    public long getCommentCount(CommunityPost post) {
        return commentRepository.countByPostAndDeletedAtIsNull(post);
    }
    
    public boolean isCommentOwner(Long commentId, User user) {
        Comment comment = getCommentById(commentId);
        return comment.getUser().getId().equals(user.getId());
    }
}
