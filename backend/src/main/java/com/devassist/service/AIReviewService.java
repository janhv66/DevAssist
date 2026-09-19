package com.devassist.service;

import com.devassist.dto.AIReviewFinding;
import com.devassist.dto.AIReviewResponse;
import com.devassist.model.Finding;
import com.devassist.model.Review;
import com.devassist.model.ReviewStatus;
import com.devassist.repository.FindingRepository;
import com.devassist.repository.ReviewRepository;
import com.devassist.service.ai.LLMProvider;
import com.devassist.service.github.GitHubDiffParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

        review.setStatus(ReviewStatus.IN_PROGRESS);
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

            review.setStatus(ReviewStatus.COMPLETED);
            review.setCompletedAt(LocalDateTime.now());

            return reviewRepository.save(review);

        } catch (Exception e) {
            review.setStatus(ReviewStatus.FAILED);
            reviewRepository.save(review);

            throw new RuntimeException("AI review failed", e);
        }
    }

    public Review runGitHubPRReview(
            String repository,
            Integer pullRequestNumber,
            String commitSha,
            List<GitHubDiffParser.ParsedDiff> files
    ) {
        Review review = new Review();

        review.setRepository(repository);
        review.setPullRequestNumber(pullRequestNumber);
        review.setCommitSha(commitSha);
        review.setProvider(llmProvider.getName());
        review.setStatus(ReviewStatus.IN_PROGRESS);
        review.setCreatedAt(LocalDateTime.now());

        review = reviewRepository.save(review);

        try {
            for (GitHubDiffParser.ParsedDiff file : files) {

                StringBuilder changedCode = new StringBuilder();

                for (GitHubDiffParser.ChangedLine line : file.changedLines()) {
                    changedCode
                            .append("Line ")
                            .append(line.lineNumber())
                            .append(": ")
                            .append(line.content())
                            .append("\n");
                }

                String prompt =
                        "File: " + file.filePath() +
                        "\n\n" +
                        "Analyze ONLY the changed lines listed below.\n" +
                        "Do not report issues from unchanged code.\n" +
                        "Do not invent files or line numbers.\n" +
                        "Only report a finding if there is a concrete, actionable issue in the changed code.\n\n" +
                        "Changed lines:\n" +
                        changedCode;

                String aiResponse = llmProvider.reviewCode(prompt);

                AIReviewResponse parsedResponse =
                        objectMapper.readValue(
                                aiResponse,
                                AIReviewResponse.class
                        );

                for (AIReviewFinding aiFinding : parsedResponse.findings()) {

                    String filePath = aiFinding.filePath();

                    if (filePath == null || filePath.isBlank()) {
                        filePath = file.filePath();
                    }

                    final String findingFilePath = filePath;

                    boolean validChangedLine = file.changedLines().stream()
                            .anyMatch(line ->
                                    line.added()
                                            && line.lineNumber() == aiFinding.lineNumber()
                                            && findingFilePath.equals(file.filePath())
                            );

                    if (!validChangedLine) {
                        continue;
                    }

                    Finding finding = new Finding();

                    finding.setReview(review);

                    finding.setFilePath(findingFilePath);
                    finding.setLineNumber(aiFinding.lineNumber());
                    finding.setCategory(aiFinding.category());
                    finding.setSeverity(aiFinding.severity());
                    finding.setTitle(aiFinding.title());
                    finding.setDescription(aiFinding.description());
                    finding.setSuggestedFix(aiFinding.suggestedFix());
                    finding.setConfidence(aiFinding.confidence());

                    findingRepository.save(finding);
                    review.addFinding(finding);
                }
            }

            review.setStatus(ReviewStatus.COMPLETED);
            review.setCompletedAt(LocalDateTime.now());

            reviewRepository.save(review);

            return reviewRepository.findById(review.getId())
                    .orElseThrow(() -> new RuntimeException("Review not found"));

        } catch (Exception e) {
            review.setStatus(ReviewStatus.FAILED);
            reviewRepository.save(review);

            throw new RuntimeException(
                    "GitHub PR AI review failed",
                    e
            );
        }
    }
}