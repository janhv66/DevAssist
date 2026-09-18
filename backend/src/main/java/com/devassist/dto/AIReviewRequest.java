package com.devassist.dto;

import jakarta.validation.constraints.NotBlank;

public class AIReviewRequest {

    @NotBlank
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}