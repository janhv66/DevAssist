package com.devassist.dto;

import java.util.List;

public record AIReviewResponse(
        List<AIReviewFinding> findings
) {
}