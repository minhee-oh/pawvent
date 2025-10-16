package com.pawvent.pawventserver.domain.enums;

//신고 처리 상태
public enum ReportStatus {
    PENDING,    // 접수됨
    REVIEWING,  // 검토 중
    RESOLVED,   // 처리 완료
    REJECTED    // 반려됨
}

