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
                You are an expert software code reviewer.

                Analyze the following code for:
                - Bugs
                - Security vulnerabilities
                - Performance issues
                - Maintainability problems

                Return ONLY valid JSON.
                Do not use Markdown.
                Do not include ```json or ```.

                The JSON must follow exactly this structure:

                {
                "findings": [
                        {
                        "filePath": "string",
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
                - category must be one of: BUG, SECURITY, PERFORMANCE, MAINTAINABILITY, STYLE
                - severity must be one of: CRITICAL, HIGH, MEDIUM, LOW
                - confidence must be between 0 and 1
                - If there are no issues, return an empty findings array.
                - Do not invent file paths or line numbers. If they are unknown, use null.
                - Return only the JSON object.

                Code to review:
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