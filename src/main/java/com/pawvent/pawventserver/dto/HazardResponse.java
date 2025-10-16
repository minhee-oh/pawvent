package com.pawvent.pawventserver.dto;

import com.pawvent.pawventserver.domain.enums.HazardCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
public class HazardResponse {
    private Long id;
    private HazardCategory category;
    private String description;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private Long reporterId;
    private String reporterNickname;
    private OffsetDateTime createdAt;
}
