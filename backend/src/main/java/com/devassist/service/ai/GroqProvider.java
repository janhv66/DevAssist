package com.devassist.service.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.devassist.dto.AIReviewResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class GroqProvider implements LLMProvider {
    private final ObjectMapper objectMapper;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GroqProvider(
        @Value("${groq.api.url}") String apiUrl,
        @Value("${groq.api.key}") String apiKey,
        @Value("${groq.model}") String model,
        ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .build();
    }

    @Override
    public String getName() {
        return "GROQ";
    }

    @Override
    public String reviewCode(String code) {
        String prompt = """
                You are an expert software engineer performing a code review.

                Review ONLY the code provided below.

                Identify concrete, actionable problems that could cause:
                - incorrect behavior or runtime errors
                - security vulnerabilities
                - performance problems
                - maintainability problems

                Be especially careful about:
                - division by zero
                - null pointer risks
                - array/index errors
                - incorrect conditions
                - resource leaks
                - unsafe input handling
                - obvious logic errors

                Do not invent problems.
                Do not invent file paths.
                Do not invent line numbers.

                If the code contains a real bug, report it.

                Return ONLY valid JSON.
                Do not use Markdown.
                Do not include ```json or ```.

                The JSON must follow exactly this structure:

                {
                "findings": [
                {
                "filePath": "string or null",
                "lineNumber": 1,
                "category": "BUG",
                "severity": "HIGH",
                "title": "string",
                "description": "string",
                "suggestedFix": "string",
                "confidence": 0.95
                }
                ]
                }

                Rules:
                - category must be one of:
                BUG, SECURITY, PERFORMANCE, MAINTAINABILITY, STYLE
                - severity must be one of:
                CRITICAL, HIGH, MEDIUM, LOW
                - confidence must be between 0 and 1
                - If there are no real issues, return an empty findings array.
                - If the file path is unknown, use null.
                - If the exact line number is unknown, use null.
                - Return only the JSON object.

                File under review:
                %s
                """.formatted(code);

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(message),
                "temperature", 0.2
        );

        Map<String, Object> response = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null) {
                throw new RuntimeException("Empty response from Groq");
        }

        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) response.get("choices");

        if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No response choices returned by Groq");
        }

        Map<String, Object> firstChoice = choices.get(0);

        Map<String, Object> responseMessage =
                (Map<String, Object>) firstChoice.get("message");

        String content = (String) responseMessage.get("content");

        try {
        AIReviewResponse parsed = objectMapper.readValue(
                content,
                AIReviewResponse.class
        );

        return objectMapper.writeValueAsString(parsed);

        } catch (Exception e) {
        throw new RuntimeException("Failed to parse Groq response as JSON", e);
        }
    }
}