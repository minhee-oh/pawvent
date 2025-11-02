package com.pawvent.pawventserver.domain;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//사용자 챌린지 참여 엔티티
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@Entity 
@Table(name = "user_challenge",uniqueConstraints = {@UniqueConstraint(name = "uq_user_challenge_user_challenge", columnNames = {"user_id", "challenge_id"})}, indexes = { @Index(name = "ix_user_challenge_user", columnList = "user_id") })
public class UserChallenge extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 현재 진행 값 */
    @Column(name = "current_value")
    private Integer currentValue = 0;

    /** 완료 여부 */
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    /** 완료 시간 */
    @Column(name = "completed_at")
    private java.time.OffsetDateTime completedAt;

    /** 삭제 시간 (소프트 삭제) */
    @Column(name = "deleted_at")
    private java.time.OffsetDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;
}
