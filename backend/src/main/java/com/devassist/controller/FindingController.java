package com.devassist.controller;

import com.devassist.dto.CreateFindingRequest;
import com.devassist.model.Finding;
import com.devassist.service.FindingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews/{reviewId}/findings")
public class FindingController {

    private final FindingService findingService;

    public FindingController(FindingService findingService) {
        this.findingService = findingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Finding createFinding(
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateFindingRequest request
    ) {
        return findingService.createFinding(reviewId, request);
    }
}