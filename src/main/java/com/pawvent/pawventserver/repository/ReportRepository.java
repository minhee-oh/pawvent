package com.pawvent.pawventserver.repository;

import com.pawvent.pawventserver.domain.Report;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.ReportStatus;
import com.pawvent.pawventserver.domain.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 신고 관련 데이터베이스 접근을 담당하는 레포지토리
 * 사용자 신고 및 관리자 처리 기능을 제공
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    /**
     * 특정 신고자의 모든 신고를 최신순으로 조회
     * @param reporter 신고자
     * @return 해당 신고자의 신고 목록 (최신순)
     */
    @Query("SELECT r FROM Report r WHERE r.reporter = :reporter ORDER BY r.createdAt DESC")
    List<Report> findByReporterOrderByCreatedAtDesc(@Param("reporter") User reporter);
    
    /**
     * 특정 상태의 모든 신고를 최신순으로 조회
     * @param status 신고 상태
     * @return 해당 상태의 신고 목록 (최신순)
     */
    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<Report> findByStatusOrderByCreatedAtDesc(@Param("status") ReportStatus status);
    
    /**
     * 특정 상태의 신고를 페이징으로 조회 (관리자용)
     * @param status 신고 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 신고 페이지
     */
    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.createdAt DESC")
    Page<Report> findByStatusOrderByCreatedAtDesc(@Param("status") ReportStatus status, Pageable pageable);
    
    /**
     * 특정 타입의 모든 신고를 최신순으로 조회
     * @param type 신고 타입
     * @return 해당 타입의 신고 목록 (최신순)
     */
    @Query("SELECT r FROM Report r WHERE r.type = :type ORDER BY r.createdAt DESC")
    List<Report> findByTypeOrderByCreatedAtDesc(@Param("type") ReportType type);
    
    /**
     * 특정 대상에 대한 모든 신고 조회
     * @param targetEntityType 대상 엔티티 타입
     * @param targetEntityId 대상 엔티티 ID
     * @return 해당 대상의 신고 목록
     */
    @Query("SELECT r FROM Report r WHERE r.targetEntityType = :targetEntityType AND r.targetEntityId = :targetEntityId ORDER BY r.createdAt DESC")
    List<Report> findByTargetEntity(@Param("targetEntityType") String targetEntityType, @Param("targetEntityId") Long targetEntityId);
    
    /**
     * 특정 처리자가 처리한 신고들 조회
     * @param processedBy 처리자
     * @return 해당 처리자가 처리한 신고 목록
     */
    @Query("SELECT r FROM Report r WHERE r.processedBy = :processedBy ORDER BY r.processedAt DESC")
    List<Report> findByProcessedByOrderByProcessedAtDesc(@Param("processedBy") User processedBy);
    
    /**
     * 특정 신고자의 신고 수 조회
     * @param reporter 신고자
     * @return 해당 신고자의 총 신고 수
     */
    long countByReporter(User reporter);
    
    /**
     * 특정 상태의 신고 수 조회
     * @param status 신고 상태
     * @return 해당 상태의 총 신고 수
     */
    long countByStatus(ReportStatus status);
    
    /**
     * 특정 타입의 신고 수 조회
     * @param type 신고 타입
     * @return 해당 타입의 총 신고 수
     */
    long countByType(ReportType type);
    
    /**
     * 특정 대상에 대한 신고 수 조회
     * @param targetEntityType 대상 엔티티 타입
     * @param targetEntityId 대상 엔티티 ID
     * @return 해당 대상의 총 신고 수
     */
    long countByTargetEntityTypeAndTargetEntityId(String targetEntityType, Long targetEntityId);
    
    /**
     * 특정 신고자의 특정 상태 신고 수 조회
     * @param reporter 신고자
     * @param status 신고 상태
     * @return 해당 조건의 신고 수
     */
    long countByReporterAndStatus(User reporter, ReportStatus status);
    
    /**
     * 미처리 신고들을 오래된 순으로 조회 (관리자용)
     * @return 처리 대기 중인 신고 목록 (오래된 순)
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    List<Report> findPendingReportsOrderByCreatedAtAsc();
    
    /**
     * 가장 많이 신고된 대상들 조회
     * @return 신고 수가 많은 대상 목록
     */
    @Query("SELECT r.targetEntityType, r.targetEntityId, COUNT(r) as reportCount FROM Report r " +
           "GROUP BY r.targetEntityType, r.targetEntityId ORDER BY COUNT(r) DESC")
    List<Object[]> findMostReportedTargets();
    
    /**
     * 특정 타입과 상태의 신고들 조회
     * @param type 신고 타입
     * @param status 신고 상태
     * @return 해당 조건의 신고 목록
     */
    @Query("SELECT r FROM Report r WHERE r.type = :type AND r.status = :status ORDER BY r.createdAt DESC")
    List<Report> findByTypeAndStatus(@Param("type") ReportType type, @Param("status") ReportStatus status);
    
    /**
     * 특정 사용자가 특정 대상을 이미 신고했는지 확인
     * @param reporter 신고자
     * @param targetEntityType 대상 엔티티 타입
     * @param targetEntityId 대상 엔티티 ID
     * @return 신고 여부
     */
    boolean existsByReporterAndTargetEntityTypeAndTargetEntityId(
            User reporter, String targetEntityType, Long targetEntityId);
    
    // ReportService에서 필요한 추가 메서드들 (deletedAt 필드 및 소프트 삭제 고려)
    boolean existsByReporterAndTypeAndTargetEntityIdAndStatus(User reporter, ReportType type, Long targetEntityId, ReportStatus status);
    boolean existsByReporterAndTypeAndTargetEntityIdAndDeletedAtIsNull(User reporter, ReportType type, Long targetEntityId);
    
    Page<Report> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
    Page<Report> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);
    Page<Report> findByTypeAndDeletedAtIsNullOrderByCreatedAtDesc(ReportType type, Pageable pageable);
    
    List<Report> findByStatusAndDeletedAtIsNullOrderByCreatedAtAsc(ReportStatus status);
    List<Report> findByReporterAndDeletedAtIsNullOrderByCreatedAtDesc(User reporter);
    List<Report> findByTypeAndTargetEntityIdAndDeletedAtIsNullOrderByCreatedAtDesc(ReportType type, Long targetEntityId);
    
    long countByStatusAndDeletedAtIsNull(ReportStatus status);
    long countByTypeAndDeletedAtIsNull(ReportType type);
    long countByReporterAndDeletedAtIsNull(User reporter);
    long countByTypeAndTargetEntityIdAndDeletedAtIsNull(ReportType type, Long targetEntityId);
    long countByDeletedAtIsNull();
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status IN :statuses AND r.deletedAt IS NULL")
    long countByStatusInAndDeletedAtIsNull(@Param("statuses") List<ReportStatus> statuses);
    
    @Query("SELECT r.targetEntityType, r.targetEntityId, COUNT(r) as reportCount FROM Report r " +
           "WHERE r.type = :type AND r.deletedAt IS NULL " +
           "GROUP BY r.targetEntityType, r.targetEntityId ORDER BY COUNT(r) DESC")
    List<Object[]> findMostReportedTargets(@Param("type") ReportType type, @Param("limit") int limit);
    
    @Query("SELECT r.reporter, COUNT(r) as reportCount FROM Report r " +
           "WHERE r.deletedAt IS NULL " +
           "GROUP BY r.reporter ORDER BY COUNT(r) DESC")
    List<Object[]> findMostReportingUsers(@Param("limit") int limit);
}



