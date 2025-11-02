package com.pawvent.pawventserver.service;

import com.pawvent.pawventserver.domain.Feedback;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.FeedbackStatus;
import com.pawvent.pawventserver.repository.FeedbackRepository;
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
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    
    @Transactional
    public Feedback createFeedback(User user, String title, String content, String email) {
        Feedback feedback = Feedback.builder()
                .user(user)
                .title(title)
                .content(content)
                .email(email)
                .status(FeedbackStatus.PENDING)
                .build();
        
        return feedbackRepository.save(feedback);
    }
    
    public Feedback getFeedbackById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다."));
    }
    
    public Page<Feedback> getAllFeedbacks(Pageable pageable) {
        return feedbackRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable);
    }
    
    public List<Feedback> getFeedbacksByUser(User user) {
        return feedbackRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user);
    }
    
    public Page<Feedback> getFeedbacksByStatus(FeedbackStatus status, Pageable pageable) {
        return feedbackRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(status, pageable);
    }
    
    public List<Feedback> getPendingFeedbacks() {
        return feedbackRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtAsc(FeedbackStatus.PENDING);
    }
    
    public List<Feedback> getInProgressFeedbacks() {
        return feedbackRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtAsc(FeedbackStatus.IN_PROGRESS);
    }
    
    @Transactional
    public Feedback updateFeedbackStatus(Long feedbackId, FeedbackStatus status, String response) {
        Feedback feedback = getFeedbackById(feedbackId);
        
        Feedback updatedFeedback = feedback.toBuilder()
                .status(status)
                .response(response)
                .respondedAt(status == FeedbackStatus.COMPLETED || status == FeedbackStatus.REJECTED ? 
                           OffsetDateTime.now() : null)
                .build();
        
        return feedbackRepository.save(updatedFeedback);
    }
    
    @Transactional
    public Feedback updateFeedback(Long feedbackId, String title, String content, String email, User user) {
        Feedback feedback = getFeedbackById(feedbackId);
        
        if (!feedback.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("피드백을 수정할 권한이 없습니다.");
        }
        
        if (feedback.getStatus() != FeedbackStatus.PENDING) {
            throw new IllegalArgumentException("처리 중이거나 완료된 피드백은 수정할 수 없습니다.");
        }
        
        Feedback updatedFeedback = feedback.toBuilder()
                .title(title)
                .content(content)
                .email(email)
                .build();
        
        return feedbackRepository.save(updatedFeedback);
    }
    
    @Transactional
    public void deleteFeedback(Long feedbackId, User user) {
        Feedback feedback = getFeedbackById(feedbackId);
        
        if (!feedback.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("피드백을 삭제할 권한이 없습니다.");
        }
        
        Feedback deletedFeedback = feedback.toBuilder()
                .deletedAt(OffsetDateTime.now())
                .build();
        
        feedbackRepository.save(deletedFeedback);
    }
    
    @Transactional
    public Feedback markAsInProgress(Long feedbackId) {
        return updateFeedbackStatus(feedbackId, FeedbackStatus.IN_PROGRESS, null);
    }
    
    @Transactional
    public Feedback completeFeedback(Long feedbackId, String response) {
        return updateFeedbackStatus(feedbackId, FeedbackStatus.COMPLETED, response);
    }
    
    @Transactional
    public Feedback rejectFeedback(Long feedbackId, String response) {
        return updateFeedbackStatus(feedbackId, FeedbackStatus.REJECTED, response);
    }
    
    public boolean isFeedbackOwner(Long feedbackId, User user) {
        Feedback feedback = getFeedbackById(feedbackId);
        return feedback.getUser().getId().equals(user.getId());
    }
    
    public long getFeedbackCountByStatus(FeedbackStatus status) {
        return feedbackRepository.countByStatusAndDeletedAtIsNull(status);
    }
    
    public long getUserFeedbackCount(User user) {
        return feedbackRepository.countByUserAndDeletedAtIsNull(user);
    }
    
    public double getFeedbackResponseRate() {
        long totalFeedbacks = feedbackRepository.countByDeletedAtIsNull();
        long respondedFeedbacks = feedbackRepository.countByStatusInAndDeletedAtIsNull(
                List.of(FeedbackStatus.COMPLETED, FeedbackStatus.REJECTED));
        
        if (totalFeedbacks == 0) {
            return 0.0;
        }
        
        return (double) respondedFeedbacks / totalFeedbacks * 100;
    }
}


