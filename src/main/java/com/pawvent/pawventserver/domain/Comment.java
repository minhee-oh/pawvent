package com.pawvent.pawventserver.domain;

import java.time.OffsetDateTime;

import com.pawvent.pawventserver.domain.common.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 댓글 엔티티
 * 
 * 커뮤니티 게시글에 달리는 댓글을 관리합니다.
 * 사용자들이 게시글에 대한 의견이나 정보를 공유할 수 있으며,
 * 소프트 삭제를 통해 삭제된 댓글도 기록으로 보관됩니다.
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
@Table(name = "comment",indexes = { @Index(name = "ix_comment_post", columnList = "post_id") })
public class Comment extends BaseTime {

    /** 댓글 고유 식별자 (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 댓글 내용 (사용자가 작성한 댓글 텍스트) */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 댓글 작성자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 댓글이 달린 게시글 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    /** 댓글 삭제 시간 (소프트 삭제를 위한 필드, null이면 활성 댓글) */
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
