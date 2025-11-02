package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.Report;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.ReportStatus;
import com.pawvent.pawventserver.domain.enums.ReportType;
import com.pawvent.pawventserver.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    
    private final ReportRepository reportRepository;
    
    @Transactional
    public Report createReport(User reporter, ReportType type, Long targetId, String reason, String description) {
        // 이미 동일한 대상에 대해 신고한 적이 있는지 확인
        boolean alreadyReported = reportRepository.existsByReporterAndTypeAndTargetEntityIdAndStatus(
                reporter, type, targetId, ReportStatus.PENDING);
        
        if (alreadyReported) {
            throw new IllegalArgumentException("이미 신고한 내용입니다.");
        }
        
        Report report = Report.builder()
                .reporter(reporter)
                .type(type)
                .targetEntityType(type.name())
                .targetEntityId(targetId)
                .reason(reason)
                .description(description)
                .status(ReportStatus.PENDING)
                .build();
        
        return reportRepository.save(report);
    }
    
    @Transactional
    public Report createPostReport(User reporter, CommunityPost post, String reason, String description) {
        return createReport(reporter, ReportType.POST, post.getId(), reason, description);
    }
    
    @Transactional
    public Report createUserReport(User reporter, User targetUser, String reason, String description) {
        if (reporter.getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("자기 자신을 신고할 수 없습니다.");
        }
        
        return createReport(reporter, ReportType.USER, targetUser.getId(), reason, description);
    }
    
    @Transactional
    public Report createCommentReport(User reporter, Long commentId, String reason, String description) {
        return createReport(reporter, ReportType.COMMENT, commentId, reason, description);
    }
    
    public Report getReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));
    }
    
    public Page<Report> getAllReports(Pageable pageable) {
        return reportRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable);
    }
    
    public Page<Report> getReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(status, pageable);
    }
    
    public Page<Report> getReportsByType(ReportType type, Pageable pageable) {
        return reportRepository.findByTypeAndDeletedAtIsNullOrderByCreatedAtDesc(type, pageable);
    }
    
    public List<Report> getPendingReports() {
        return reportRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtAsc(ReportStatus.PENDING);
    }
    
    public List<Report> getReportsByReporter(User reporter) {
        return reportRepository.findByReporterAndDeletedAtIsNullOrderByCreatedAtDesc(reporter);
    }
    
    public List<Report> getReportsByTargetId(ReportType type, Long targetId) {
        return reportRepository.findByTypeAndTargetEntityIdAndDeletedAtIsNullOrderByCreatedAtDesc(type, targetId);
    }
    
    @Transactional
    public Report updateReportStatus(Long reportId, ReportStatus status, String adminComment) {
        Report report = getReportById(reportId);
        
        Report updatedReport = report.toBuilder()
                .status(status)
                .adminResponse(adminComment)
                .processedAt(OffsetDateTime.now())
                .build();
        
        return reportRepository.save(updatedReport);
    }
    
    @Transactional
    public Report approveReport(Long reportId, String adminComment) {
        return updateReportStatus(reportId, ReportStatus.APPROVED, adminComment);
    }
    
    @Transactional
    public Report rejectReport(Long reportId, String adminComment) {
        return updateReportStatus(reportId, ReportStatus.REJECTED, adminComment);
    }
    
    @Transactional
    public Report markAsInProgress(Long reportId, String adminComment) {
        return updateReportStatus(reportId, ReportStatus.IN_PROGRESS, adminComment);
    }
    
    @Transactional
    public void deleteReport(Long reportId) {
        Report report = getReportById(reportId);
        
        Report deletedReport = report.toBuilder()
                .deletedAt(OffsetDateTime.now())
                .build();
        
        reportRepository.save(deletedReport);
    }
    
    public long getReportCount(ReportStatus status) {
        return reportRepository.countByStatusAndDeletedAtIsNull(status);
    }
    
    public long getReportCountByType(ReportType type) {
        return reportRepository.countByTypeAndDeletedAtIsNull(type);
    }
    
    public long getReportCountByReporter(User reporter) {
        return reportRepository.countByReporterAndDeletedAtIsNull(reporter);
    }
    
    public long getReportCountByTarget(ReportType type, Long targetId) {
        return reportRepository.countByTypeAndTargetEntityIdAndDeletedAtIsNull(type, targetId);
    }
    
    public boolean hasUserReportedTarget(User reporter, ReportType type, Long targetId) {
        return reportRepository.existsByReporterAndTypeAndTargetEntityIdAndDeletedAtIsNull(reporter, type, targetId);
    }
    
    /**
     * 특정 대상에 대한 신고 횟수가 임계치를 초과했는지 확인
     */
    public boolean isReportThresholdExceeded(ReportType type, Long targetId, int threshold) {
        long reportCount = getReportCountByTarget(type, targetId);
        return reportCount >= threshold;
    }
    
    /**
     * 신고 처리율 계산 (처리된 신고 / 전체 신고)
     */
    public double getReportProcessingRate() {
        long totalReports = reportRepository.countByDeletedAtIsNull();
        long processedReports = reportRepository.countByStatusInAndDeletedAtIsNull(
                List.of(ReportStatus.APPROVED, ReportStatus.REJECTED));
        
        if (totalReports == 0) {
            return 0.0;
        }
        
        return (double) processedReports / totalReports * 100;
    }
    
    /**
     * 가장 많이 신고된 대상들 조회
     */
    public List<Object[]> getMostReportedTargets(ReportType type, int limit) {
        return reportRepository.findMostReportedTargets(type, limit);
    }
    
    /**
     * 신고가 가장 많은 사용자들 조회
     */
    public List<Object[]> getMostReportingUsers(int limit) {
        return reportRepository.findMostReportingUsers(limit);
    }
}


