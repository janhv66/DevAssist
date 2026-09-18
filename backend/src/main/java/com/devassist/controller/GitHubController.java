package com.devassist.controller;

import com.devassist.service.github.GitHubService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
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
}