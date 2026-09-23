package com.codeassess.backend.ai;

import com.codeassess.backend.ai.dto.GeminiResponse;
import com.codeassess.backend.config.AIProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Objects;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiAIProvider implements AIProvider {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAIProvider.class);

    private final RestTemplate restTemplate;
    private final AIProperties aiProperties;
    private final Timer reviewTimer;
    private final Counter failureCounter;

    public GeminiAIProvider(RestTemplate restTemplate, AIProperties aiProperties, MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.aiProperties = aiProperties;
        this.reviewTimer = meterRegistry.timer("codeassess.ai.latency", "provider", "gemini");
        this.failureCounter = meterRegistry.counter("codeassess.ai.failures", "provider", "gemini");
    }

    @Override
    public String reviewCode(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be blank");
        }

        if (!aiProperties.isReviewEnabled()) {
            throw new IllegalStateException("AI review is disabled. Set AI_REVIEW_ENABLED=true to enable it.");
        }

        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("AI API key is not configured.");
        }

        String sanitizedPrompt = prompt.trim();
        int maxLength = aiProperties.getMaxPromptLength();
        if (maxLength > 0 && sanitizedPrompt.length() > maxLength) {
            throw new IllegalArgumentException(
                    "Prompt exceeds the configured maximum length of " + maxLength + " characters"
            );
        }

        String url = aiProperties.getBaseUrl()
                + "/v1beta/models/"
                + aiProperties.getModel()
                + ":generateContent";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", aiProperties.getApiKey());

        GeminiRequest requestBody = new GeminiRequest();
        GeminiRequest.Content content = new GeminiRequest.Content();
        GeminiRequest.Part part = new GeminiRequest.Part();
        part.setText(sanitizedPrompt);
        content.setParts(java.util.List.of(part));
        requestBody.setContents(java.util.List.of(content));

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<GeminiResponse> response = reviewTimer.record(() -> 
                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    GeminiResponse.class
                )
            );

            GeminiResponse body = response.getBody();
            if (body == null || body.getCandidates() == null || body.getCandidates().isEmpty()) {
                throw new IllegalStateException("No content returned by Gemini API");
            }

            String generatedText = body.getCandidates().stream()
                    .map(candidate -> candidate.getContent())
                    .filter(Objects::nonNull)
                    .map(contentBlock -> contentBlock.getParts())
                    .filter(Objects::nonNull)
                    .flatMap(java.util.Collection::stream)
                    .map(GeminiResponse.Part::getText)
                    .filter(Objects::nonNull)
                    .reduce((first, second) -> second)
                    .orElseThrow(() -> new IllegalStateException("Gemini response did not contain text"));

            logger.info("Gemini review generated successfully for model {}", aiProperties.getModel());
            return generatedText;

        } catch (HttpClientErrorException e) {
            failureCounter.increment();
            logger.error("Gemini API request failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("Gemini API request failed: " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            failureCounter.increment();
            logger.error("Gemini API communication failed: {}", e.getMessage());
            throw new IllegalStateException("Failed to communicate with Gemini API", e);
        }
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    public static class GeminiRequest {
        private java.util.List<Content> contents;

        public java.util.List<Content> getContents() {
            return contents;
        }

        public void setContents(java.util.List<Content> contents) {
            this.contents = contents;
        }

        public static class Content {
            private java.util.List<Part> parts;

            public java.util.List<Part> getParts() {
                return parts;
            }

            public void setParts(java.util.List<Part> parts) {
                this.parts = parts;
            }
        }

        public static class Part {
            private String text;

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }
        }
    }
}
