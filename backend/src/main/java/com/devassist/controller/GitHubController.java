package com.devassist.controller;

import com.devassist.dto.ReviewResponse;
import com.devassist.service.AIReviewService;
import com.devassist.service.github.GitHubCommentService;
import com.devassist.service.github.GitHubDiffParser;
import com.devassist.service.github.GitHubPullRequest;
import com.devassist.service.github.GitHubService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService gitHubService;
    private final AIReviewService aiReviewService;
    private final GitHubCommentService gitHubCommentService;

    public GitHubController(
            GitHubService gitHubService,
            AIReviewService aiReviewService,
            GitHubCommentService gitHubCommentService
    ) {
        this.gitHubService = gitHubService;
        this.aiReviewService = aiReviewService;
        this.gitHubCommentService = gitHubCommentService;
    }

    @GetMapping("/repos/{owner}/{repo}/pulls/{pullNumber}/diff")
    public String getPullRequestDiff(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable int pullNumber
    ) {
        return gitHubService.getPullRequestDiff(
                owner,
                repo,
                pullNumber
        );
    }

    @PostMapping("/repos/{owner}/{repo}/pulls/{pullNumber}/review")
    public ReviewResponse reviewPullRequest(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable int pullNumber
    ) {
        String diff = gitHubService.getPullRequestDiff(
                owner,
                repo,
                pullNumber
        );

        GitHubPullRequest pullRequest =
                gitHubService.getPullRequest(
                        owner,
                        repo,
                        pullNumber
                );

        GitHubDiffParser parser = new GitHubDiffParser();

        var files = parser.parse(diff);

        System.out.println("Parsed files: " + files.size());

        for (var file : files) {
            System.out.println("FILE: " + file.filePath());
            System.out.println("CODE:");
            System.out.println(file.code());
        }

        var review = aiReviewService.runGitHubPRReview(
                owner + "/" + repo,
                pullNumber,
                pullRequest.headSha(),
                files
        );
        for (var finding : review.getFindings()) {
            gitHubCommentService.postInlineComment(
                    owner,
                    repo,
                    pullNumber,
                    review.getCommitSha(),
                    finding
            );
        }

        return new ReviewResponse(
                review.getId(),
                review.getRepository(),
                review.getPullRequestNumber(),
                review.getCommitSha(),
                review.getStatus(),
                review.getProvider(),
                review.getCreatedAt(),
                review.getCompletedAt(),
                review.getFindings()
                        .stream()
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