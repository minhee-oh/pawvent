package com.pawvent.pawventserver.domain.enums;

//알림 타입
public enum NotificationType {
    HAZARD_NEARBY,      // 위험 스팟 근처 알림
    POST_COMMENT,       // 내 게시글에 댓글 알림
    POST_LIKE,          // 내 게시글에 좋아요 알림
    CHALLENGE,          // 챌린지 관련 알림
    FEEDBACK_RESPONSE,  // 피드백 응답 알림
    SYSTEM              // 시스템 공지사항
}
