package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.PostCategory;
import com.pawvent.pawventserver.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;

    @Transactional
    public CommunityPost create(User user, String title, String content, PostCategory category, String imageUrl, String videoUrl) {
        CommunityPost post = CommunityPost.builder()
                .user(user)
                .title(title)
                .content(content)
                .category(category)
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
                .commentsCount(0)
                .likesCount(0)
                .viewCount(0)
                .build();
        return communityPostRepository.save(post);
    }

    public Page<CommunityPost> list(PostCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        return communityPostRepository.findByCategoryNullable(category, pageable);
    }

    public CommunityPost getByIdWithUser(Long id) {
        return communityPostRepository.findByIdWithUser(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    public CommunityPost getPostById(Long id) {
        return communityPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    @Transactional
    public CommunityPost update(Long postId, User requester, String title, String content, PostCategory category, String imageUrl, String videoUrl) {
        CommunityPost post = getByIdWithUser(postId);
        if (!post.getUser().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
        }
        CommunityPost.CommunityPostBuilder builder = post.toBuilder();
        if (title != null && !title.trim().isEmpty()) builder.title(title.trim());
        if (content != null && !content.trim().isEmpty()) builder.content(content.trim());
        if (category != null) builder.category(category);
        if (imageUrl != null) builder.imageUrl(imageUrl);
        if (videoUrl != null) builder.videoUrl(videoUrl);
        CommunityPost updated = builder.build();
        return communityPostRepository.save(updated);
    }

    @Transactional
    public void delete(Long postId, User requester) {
        CommunityPost post = getByIdWithUser(postId);
        if (!post.getUser().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다.");
        }
        post.setDeletedAt(LocalDateTime.now());
        communityPostRepository.save(post);
    }

    @Transactional
    public void incrementLikeCount(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.setLikesCount(post.getLikesCount() + 1);
        communityPostRepository.save(post);
    }

    @Transactional
    public void decrementLikeCount(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (post.getLikesCount() > 0) {
            post.setLikesCount(post.getLikesCount() - 1);
            communityPostRepository.save(post);
        }
    }

    @Transactional
    public void incrementCommentCount(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.setCommentsCount(post.getCommentsCount() + 1);
        communityPostRepository.save(post);
    }

    @Transactional
    public void decrementCommentCount(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (post.getCommentsCount() > 0) {
            post.setCommentsCount(post.getCommentsCount() - 1);
            communityPostRepository.save(post);
        }
    }
}
