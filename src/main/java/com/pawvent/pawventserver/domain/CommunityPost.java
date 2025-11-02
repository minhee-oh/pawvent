package com.pawvent.pawventserver.domain;

import java.time.OffsetDateTime;

import com.pawvent.pawventserver.domain.common.BaseTime;
import com.pawvent.pawventserver.domain.enums.PostCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 커뮤니티 게시글 엔티티
 * 
 * 사용자들이 반려동물 관련 정보를 공유하고 소통하는 
 * 커뮤니티 게시판의 게시글을 관리합니다.
 * 카테고리별로 분류되며, 좋아요와 댓글 기능을 제공합니다.
 * 성능 최적화를 위해 좋아요 수와 댓글 수를 캐시합니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@Entity 
@Table(name = "community_post",indexes = {@Index(name = "ix_post_user", columnList = "user_id"), @Index(name = "ix_post_category_created", columnList = "category, created_at")})
public class CommunityPost extends BaseTime {

    /** 게시글 고유 식별자 (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 게시글 제목 */
    @Column(length = 200, nullable = false)
    private String title;

    /** 게시글 본문 내용 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 게시글 카테고리 (산책, 건강, 훈련, 자유게시판 등) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory category;

    /** 게시글 작성자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 게시글 좋아요 수 (성능 최적화를 위한 캐시 필드) */
    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;
    
    /** 게시글 조회 수 */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    /** 게시글 댓글 수 (성능 최적화를 위한 캐시 필드) */
    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount = 0;

    /** 게시글 삭제 시간 (소프트 삭제를 위한 필드, null이면 활성 게시글) */
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
