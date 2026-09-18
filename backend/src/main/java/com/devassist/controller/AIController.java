package com.devassist.controller;

import com.devassist.dto.AIReviewRequest;
import com.devassist.dto.ReviewResponse;
import com.devassist.service.AIReviewService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class AIController {

    private final AIReviewService aiReviewService;

    public AIController(AIReviewService aiReviewService) {
        this.aiReviewService = aiReviewService;
    }

    @PostMapping("/{reviewId}/ai-review")
    public ReviewResponse runAIReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody AIReviewRequest request
    ) {
        var review = aiReviewService.runAIReview(
                reviewId,
                request.getCode()
        );

        return new ReviewResponse(
                review.getId(),
                review.getRepository(),
                review.getPullRequestNumber(),
                review.getCommitSha(),
                review.getStatus(),
                review.getProvider(),
                review.getCreatedAt(),
                review.getCompletedAt(),
                review.getFindings().stream()
                        .map(finding -> new com.devassist.dto.FindingResponse(
                                finding.getId(),
                                finding.getFilePath(),
                                finding.getLineNumber(),
                                finding.getCategory(),
                                finding.getSeverity(),
                                finding.getTitle(),
                                finding.getDescription(),
                                finding.getSuggestedFix(),
                                finding.getConfidence()
                        ))
                        .toList()
        );
    }
}