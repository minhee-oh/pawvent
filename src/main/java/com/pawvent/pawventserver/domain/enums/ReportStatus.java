package com.pawvent.pawventserver.domain.enums;

// 신고 처리 상태 (서비스/레포지토리와 일치)
public enum ReportStatus {
    PENDING,        // 접수됨
    IN_PROGRESS,    // 처리 중(검토)
    APPROVED,       // 승인/처리 완료
    REJECTED        // 반려됨
}

