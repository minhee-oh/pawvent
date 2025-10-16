package com.pawvent.pawventserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private Long authorId;
    private String authorNickname;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
