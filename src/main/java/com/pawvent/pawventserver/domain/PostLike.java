package com.pawvent.pawventserver.domain;

import com.pawvent.pawventserver.domain.common.BaseTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시글 좋아요 엔티티
 * 
 * 사용자가 커뮤니티 게시글에 표시한 좋아요 정보를 관리합니다.
 * 사용자당 게시글당 하나의 좋아요만 가능하도록 제약조건이 설정되어 있습니다.
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
@Table(name = "post_like",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_post_like_user_post", columnNames = {"user_id", "post_id"})
    },
    indexes = {
        @Index(name = "ix_post_like_user", columnList = "user_id"),
        @Index(name = "ix_post_like_post", columnList = "post_id")
    }
)
public class PostLike extends BaseTime {

    /** 좋아요 고유 식별자 (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 좋아요를 누른 사용자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 좋아요가 눌린 게시글 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;
}

