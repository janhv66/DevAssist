package com.devassist.service.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubService {

    private final RestClient restClient;
    private final String token;
    private final ObjectMapper objectMapper;

    public GitHubService(
            @Value("${github.api.url}") String apiUrl,
            @Value("${github.token}") String token,
            ObjectMapper objectMapper
    ) {
        this.token = token;
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("X-GitHub-Api-Version", "2026-03-10")
                .defaultHeader("User-Agent", "DevAssist")
                .build();
    }

    public String getPullRequestDiff(
            String owner,
            String repository,
            int pullRequestNumber
    ) {
        return restClient.get()
                .uri("/repos/{owner}/{repo}/pulls/{pullNumber}",
                        owner,
                        repository,
                        pullRequestNumber)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github.diff")
                .retrieve()
                .body(String.class);
    }

    public GitHubPullRequest getPullRequest(
            String owner,
            String repository,
            int pullRequestNumber
    ) {
        String response = restClient.get()
                .uri("/repos/{owner}/{repo}/pulls/{pullNumber}",
                        owner,
                        repository,
                        pullRequestNumber)
                .header("Authorization", "Bearer " + token)
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .body(String.class);

        try {
            JsonNode json = objectMapper.readTree(response);

            return new GitHubPullRequest(
                    json.path("head").path("sha").asText()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse GitHub pull request response",
                    e
            );
        }
    }
}