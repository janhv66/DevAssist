package com.devassist.service.github;

import com.devassist.model.Finding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class GitHubCommentService {

    private final RestClient restClient;
    private final String token;

    public GitHubCommentService(
            @Value("${github.api.url}") String apiUrl,
            @Value("${github.token}") String token
    ) {
        this.token = token;

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("X-GitHub-Api-Version", "2026-03-10")
                .defaultHeader("User-Agent", "DevAssist")
                .build();
    }

    public void postInlineComment(
            String owner,
            String repository,
            int pullRequestNumber,
            String commitSha,
            Finding finding
    ) {
        String body = """
                **DevAssist AI Review**

                **%s — %s**

                **%s**

                %s

                **Suggested fix:**
                %s

                **Confidence:** %.2f
                """.formatted(
                finding.getSeverity(),
                finding.getCategory(),
                finding.getTitle(),
                finding.getDescription(),
                finding.getSuggestedFix(),
                finding.getConfidence()
        );

        Map<String, Object> request = Map.of(
                "body", body,
                "commit_id", commitSha,
                "path", finding.getFilePath(),
                "line", finding.getLineNumber(),
                "side", "RIGHT"
        );

        restClient.post()
                .uri(
                        "/repos/{owner}/{repo}/pulls/{pullNumber}/comments",
                        owner,
                        repository,
                        pullRequestNumber
                )
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}