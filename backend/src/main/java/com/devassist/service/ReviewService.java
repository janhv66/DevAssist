package com.devassist.service;

import java.util.List;
import com.devassist.dto.FindingResponse;
import com.devassist.dto.ReviewResponse;
import com.devassist.model.Review;
import com.devassist.model.ReviewStatus;
import com.devassist.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review createReview(
            String repository,
            Integer pullRequestNumber,
            String commitSha,
            String provider
    ) {
        Review review = new Review();

        review.setRepository(repository);
        review.setPullRequestNumber(pullRequestNumber);
        review.setCommitSha(commitSha);
        review.setProvider(provider);
        review.setStatus(ReviewStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public ReviewResponse getReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        return toResponse(review);
    }

    private ReviewResponse toResponse(Review review) {
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
                .map(finding -> new FindingResponse(
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
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }
}