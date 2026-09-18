package com.devassist.service.github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubService {

    private final RestClient restClient;
    private final String token;

    public GitHubService(
            @Value("${github.api.url}") String apiUrl,
            @Value("${github.token}") String token
    ) {
        this.token = token;

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Accept", "application/vnd.github.diff")
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
                .retrieve()
                .body(String.class);
    }
}