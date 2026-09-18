package com.devassist.service;

import com.devassist.dto.CreateFindingRequest;
import com.devassist.model.Finding;
import com.devassist.model.Review;
import com.devassist.repository.FindingRepository;
import com.devassist.repository.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
public class FindingService {

    private final FindingRepository findingRepository;
    private final ReviewRepository reviewRepository;

    public FindingService(
            FindingRepository findingRepository,
            ReviewRepository reviewRepository
    ) {
        this.findingRepository = findingRepository;
        this.reviewRepository = reviewRepository;
    }

    public Finding createFinding(
            Long reviewId,
            CreateFindingRequest request
    ) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Finding finding = new Finding();

        finding.setReview(review);
        finding.setFilePath(request.getFilePath());
        finding.setLineNumber(request.getLineNumber());
        finding.setCategory(request.getCategory());
        finding.setSeverity(request.getSeverity());
        finding.setTitle(request.getTitle());
        finding.setDescription(request.getDescription());
        finding.setSuggestedFix(request.getSuggestedFix());
        finding.setConfidence(request.getConfidence());

        return findingRepository.save(finding);
    }
}