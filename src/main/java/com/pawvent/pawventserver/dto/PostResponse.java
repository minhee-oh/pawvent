package com.pawvent.pawventserver.dto;

import com.pawvent.pawventserver.domain.enums.PostCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 커뮤니티 게시글 응답 DTO
 * 
 * 클라이언트에게 전달되는 게시글 정보를 담는 데이터 전송 객체입니다.
 * 게시글 목록 조회, 상세 조회 등에서 활용됩니다.
 * 
 * @author Pawvent Team
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@AllArgsConstructor
public class PostResponse {
    /** 게시글 고유 식별자 */
    private Long id;
    
    /** 게시글 제목 */
    private String title;
    
    /** 게시글 내용 */
    private String content;
    
    /** 게시글 카테고리 */
    private PostCategory category;
    
    /** 조회 수 */
    private Integer viewCount;
    
    /** 좋아요 수 */
    private Integer likeCount;
    
    /** 작성자 ID */
    private Long authorId;
    
    /** 작성자 닉네임 */
    private String authorNickname;
    
    /** 작성일 */
    private OffsetDateTime createdAt;
    
    /** 최종 수정일 */
    private OffsetDateTime updatedAt;
}
