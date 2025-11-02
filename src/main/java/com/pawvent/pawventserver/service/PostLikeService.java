package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.PostLike;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {
    
    private final PostLikeRepository postLikeRepository;
    private final CommunityPostService communityPostService;
    private final NotificationService notificationService;
    
    @Transactional
    public PostLike toggleLike(User user, CommunityPost post) {
        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);
        
        if (existingLike.isPresent()) {
            // 이미 좋아요가 있다면 삭제 (좋아요 취소)
            postLikeRepository.delete(existingLike.get());
            communityPostService.decrementLikeCount(post.getId());
            return null;
        } else {
            // 좋아요가 없다면 새로 생성
            PostLike postLike = PostLike.builder()
                    .user(user)
                    .post(post)
                    .build();
            
            PostLike savedLike = postLikeRepository.save(postLike);
            communityPostService.incrementLikeCount(post.getId());
            
            // 게시글 작성자에게 좋아요 알림 발송 (본인이 아닌 경우에만)
            if (!post.getUser().getId().equals(user.getId())) {
                notificationService.createLikeNotification(
                    post.getUser(), 
                    post.getTitle(), 
                    user.getNickname()
                );
            }
            
            return savedLike;
        }
    }
    
    @Transactional
    public PostLike addLike(User user, CommunityPost post) {
        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);
        
        if (existingLike.isPresent()) {
            throw new IllegalArgumentException("이미 좋아요를 누른 게시글입니다.");
        }
        
        PostLike postLike = PostLike.builder()
                .user(user)
                .post(post)
                .build();
        
        PostLike savedLike = postLikeRepository.save(postLike);
        communityPostService.incrementLikeCount(post.getId());
        
        // 게시글 작성자에게 좋아요 알림 발송 (본인이 아닌 경우에만)
        if (!post.getUser().getId().equals(user.getId())) {
            notificationService.createLikeNotification(
                post.getUser(), 
                post.getTitle(), 
                user.getNickname()
            );
        }
        
        return savedLike;
    }
    
    @Transactional
    public void removeLike(User user, CommunityPost post) {
        PostLike postLike = postLikeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new IllegalArgumentException("좋아요를 찾을 수 없습니다."));
        
        postLikeRepository.delete(postLike);
        communityPostService.decrementLikeCount(post.getId());
    }
    
    @Transactional
    public void removeLikeById(Long postLikeId, User user) {
        PostLike postLike = postLikeRepository.findById(postLikeId)
                .orElseThrow(() -> new IllegalArgumentException("좋아요를 찾을 수 없습니다."));
        
        if (!postLike.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("좋아요를 삭제할 권한이 없습니다.");
        }
        
        postLikeRepository.delete(postLike);
        communityPostService.decrementLikeCount(postLike.getPost().getId());
    }
    
    public boolean isLikedByUser(User user, CommunityPost post) {
        return postLikeRepository.findByUserAndPost(user, post).isPresent();
    }
    
    public long getLikeCount(CommunityPost post) {
        return postLikeRepository.countByPost(post);
    }
    
    public List<PostLike> getLikesByPost(CommunityPost post) {
        return postLikeRepository.findByPostOrderByCreatedAtDesc(post);
    }
    
    public List<PostLike> getLikesByUser(User user) {
        return postLikeRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<CommunityPost> getLikedPostsByUser(User user) {
        return postLikeRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(PostLike::getPost)
                .toList();
    }
    
    public List<User> getUsersWhoLikedPost(CommunityPost post) {
        return postLikeRepository.findByPostOrderByCreatedAtDesc(post)
                .stream()
                .map(PostLike::getUser)
                .toList();
    }
    
    @Transactional
    public void deleteLikesByPost(CommunityPost post) {
        List<PostLike> postLikes = postLikeRepository.findByPost(post);
        postLikeRepository.deleteAll(postLikes);
    }
    
    @Transactional
    public void deleteLikesByUser(User user) {
        List<PostLike> postLikes = postLikeRepository.findByUser(user);
        postLikeRepository.deleteAll(postLikes);
    }
    
    public long getUserLikeCount(User user) {
        return postLikeRepository.countByUser(user);
    }
    
    /**
     * 사용자가 받은 총 좋아요 수 (자신이 작성한 게시글에 받은 좋아요)
     */
    public long getUserReceivedLikeCount(User user) {
        return postLikeRepository.countReceivedLikesByAuthor(user);
    }
    
    /**
     * PostLike가 유효한지 확인 (사용자와 게시글이 모두 존재하고 삭제되지 않았는지)
     */
    public boolean isValidPostLike(Long postLikeId) {
        Optional<PostLike> postLike = postLikeRepository.findById(postLikeId);
        return postLike.isPresent() && 
               postLike.get().getUser().getDeletedAt() == null &&
               postLike.get().getPost().getDeletedAt() == null;
    }
}


