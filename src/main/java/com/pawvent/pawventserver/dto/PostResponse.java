package com.pawvent.pawventserver.dto;

import com.pawvent.pawventserver.domain.enums.PostCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private PostCategory category;
    private Integer likesCount;
    private Integer commentsCount;
    private Long authorId;
    private String authorNickname;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
