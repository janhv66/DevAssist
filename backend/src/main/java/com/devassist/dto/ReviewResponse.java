package com.devassist.dto;

import com.devassist.model.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long id,
        String repository,
        Integer pullRequestNumber,
        String commitSha,
        ReviewStatus status,
        String provider,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        List<FindingResponse> findings
) {
}