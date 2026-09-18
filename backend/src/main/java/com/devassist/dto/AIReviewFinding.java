package com.devassist.dto;

import com.devassist.model.FindingCategory;
import com.devassist.model.FindingSeverity;

public record AIReviewFinding(
        String filePath,
        Integer lineNumber,
        FindingCategory category,
        FindingSeverity severity,
        String title,
        String description,
        String suggestedFix,
        Double confidence
) {
}