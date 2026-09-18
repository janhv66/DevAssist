package com.devassist.service;

import com.devassist.dto.AIReviewFinding;
import com.devassist.dto.AIReviewResponse;
import com.devassist.model.Finding;
import com.devassist.model.Review;
import com.devassist.repository.FindingRepository;
import com.devassist.repository.ReviewRepository;
import com.devassist.service.ai.LLMProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AIReviewService {

    private final ReviewRepository reviewRepository;
    private final FindingRepository findingRepository;
    private final LLMProvider llmProvider;
    private final ObjectMapper objectMapper;

    public AIReviewService(
            ReviewRepository reviewRepository,
            FindingRepository findingRepository,
            LLMProvider llmProvider,
            ObjectMapper objectMapper
    ) {
        this.reviewRepository = reviewRepository;
        this.findingRepository = findingRepository;
        this.llmProvider = llmProvider;
        this.objectMapper = objectMapper;
    }

    public Review runAIReview(Long reviewId, String code) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setStatus(com.devassist.model.ReviewStatus.IN_PROGRESS);
        reviewRepository.save(review);

        try {
            String aiResponse = llmProvider.reviewCode(code);

            AIReviewResponse parsedResponse =
                    objectMapper.readValue(aiResponse, AIReviewResponse.class);

            for (AIReviewFinding aiFinding : parsedResponse.findings()) {
                Finding finding = new Finding();

                finding.setReview(review);
                finding.setFilePath(aiFinding.filePath());
                finding.setLineNumber(aiFinding.lineNumber());
                finding.setCategory(aiFinding.category());
                finding.setSeverity(aiFinding.severity());
                finding.setTitle(aiFinding.title());
                finding.setDescription(aiFinding.description());
                finding.setSuggestedFix(aiFinding.suggestedFix());
                finding.setConfidence(aiFinding.confidence());

                findingRepository.save(finding);
            }

            review.setStatus(com.devassist.model.ReviewStatus.COMPLETED);
            review.setCompletedAt(java.time.LocalDateTime.now());

            return reviewRepository.save(review);

        } catch (Exception e) {
            review.setStatus(com.devassist.model.ReviewStatus.FAILED);
            reviewRepository.save(review);

            throw new RuntimeException("AI review failed", e);
        }
    }
}