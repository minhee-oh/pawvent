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

//게시글 좋아요 엔티티
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;
}
