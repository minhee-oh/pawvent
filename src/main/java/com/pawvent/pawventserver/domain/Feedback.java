package com.pawvent.pawventserver.domain;

import com.pawvent.pawventserver.domain.common.BaseTime;
import com.pawvent.pawventserver.domain.enums.FeedbackStatus;

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

//피드백/정정 요청 엔티티
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@Entity 
@Table(name = "feedback",indexes = {@Index(name = "ix_feedback_status", columnList = "status"),@Index(name = "ix_feedback_hazard", columnList = "hazard_id")})
public class Feedback extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 피드백 제목 */
    @Column(length = 200, nullable = false)
    private String title;

    /** 피드백 내용 */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 연락받을 이메일 주소 */
    @Column(length = 100)
    private String email;

    /** 피드백 처리 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackStatus status;

    /** 관리자 응답 */
    @Column(columnDefinition = "TEXT")
    private String response;

    /** 응답 완료 시간 */
    @Column(name = "responded_at")
    private java.time.OffsetDateTime respondedAt;

    /** 삭제 시간 (소프트 삭제) */
    @Column(name = "deleted_at")
    private java.time.OffsetDateTime deletedAt;

    /** 피드백 작성자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 피드백 대상 위험요소 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hazard_id", nullable = false)
    private Hazard hazard;
}
