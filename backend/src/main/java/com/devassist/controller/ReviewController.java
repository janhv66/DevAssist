package com.devassist.controller;

import java.util.List;
import com.devassist.dto.CreateReviewRequest;
import com.devassist.dto.ReviewResponse;
import com.devassist.model.Review;
import com.devassist.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review createReview(
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return reviewService.createReview(
                request.getRepository(),
                request.getPullRequestNumber(),
                request.getCommitSha(),
                request.getProvider()
        );
    }
    @GetMapping("/{id}")
    public ReviewResponse getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }
    @GetMapping
    public List<ReviewResponse> getAllReviews() {
        return reviewService.getAllReviews();
    }
}