package com.pawvent.pawventserver.domain.enums;

// 피드백 처리 상태
public enum FeedbackStatus {
    PENDING,        // 접수됨(대기)
    IN_PROGRESS,    // 처리 중
    COMPLETED,      // 처리 완료
    REJECTED        // 반려
}
