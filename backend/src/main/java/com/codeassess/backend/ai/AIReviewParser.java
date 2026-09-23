package com.codeassess.backend.ai;

import com.codeassess.backend.ai.dto.AIReviewPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class AIReviewParser {

    private final ObjectMapper objectMapper;

    public AIReviewParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AIReviewPayload parse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalArgumentException("AI provider returned an empty response");
        }

        String json = extractJson(rawResponse);

        try {
            AIReviewPayload payload = objectMapper.readValue(json, AIReviewPayload.class);
            validate(payload);
            return payload;
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("AI provider returned invalid review JSON", exception);
        }
    }

    private String extractJson(String response) {
        String cleaned = response
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');

        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("AI response does not contain valid JSON");
        }

        return cleaned.substring(start, end + 1);
    }

    private void validate(AIReviewPayload payload) {
        if (payload.getOverallScore() == null) {
            throw new IllegalArgumentException("AI review score is missing");
        }

        if (payload.getOverallScore() < 0 || payload.getOverallScore() > 100) {
            throw new IllegalArgumentException("AI review score must be between 0 and 100");
        }

        if (payload.getSummary() == null || payload.getSummary().isBlank()) {
            throw new IllegalArgumentException("AI review summary is missing");
        }
    }
}
