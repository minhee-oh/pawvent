package com.pawvent.pawventserver.dto.community;

import com.pawvent.pawventserver.domain.CommunityPost;
import com.pawvent.pawventserver.domain.User;
import com.pawvent.pawventserver.domain.enums.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

public class CommunityPostDtos {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
        @NotNull
        private PostCategory category;
        private String imageUrl;
        private String videoUrl;
    }

    @Data
    public static class UpdateRequest {
        private String title;
        private String content;
        private PostCategory category;
        private String imageUrl;
        private String videoUrl;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private PostCategory category;
        private String imageUrl;
        private String videoUrl;
        private Long authorId;
        private String authorNickname;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(CommunityPost post, String authorNickname) {
            User user = post.getUser();
            return Response.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .category(post.getCategory())
                    .imageUrl(post.getImageUrl())
                    .videoUrl(post.getVideoUrl())
                    .authorId(user.getId())
                    .authorNickname(authorNickname)
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .build();
        }
    }
}



