package com.codeassess.backend.ai;

import com.codeassess.backend.ai.dto.GeminiResponse;
import com.codeassess.backend.config.AIProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;

import java.net.SocketTimeoutException;

class AIProviderTest {

    @Test
    void mockProviderReturnsStructuredReview() {
        AIProvider provider = new MockAIProvider();

        String response = provider.reviewCode("public class Test {}\n");

        assertNotNull(response);
        assertTrue(response.contains("overallScore"));
        assertTrue(response.contains("strengths"));
    }

    @Test
    void geminiProviderExtractsTextFromGeminiResponse() {
        AIProperties properties = new AIProperties();
        properties.setReviewEnabled(true);
        properties.setApiKey("test-key");
        properties.setBaseUrl("https://generativelanguage.googleapis.com");
        properties.setModel("gemini-2.5-flash");

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"{\\n  \\\"overallScore\\\": 88,\\n  \\\"summary\\\": \\\"Good solution\\\"\\n}\"}],\"role\":\"model\"}}]}",
                                MediaType.APPLICATION_JSON));

        GeminiAIProvider provider = new GeminiAIProvider(restTemplate, properties, new io.micrometer.core.instrument.simple.SimpleMeterRegistry());
        String result = provider.reviewCode("public class Test {}\n");

        assertEquals("{\n  \"overallScore\": 88,\n  \"summary\": \"Good solution\"\n}", result);
        server.verify();
    }

    @Test
    void geminiProviderFallsBackWhenNoCandidateIsReturned() {
        AIProperties properties = enabledProperties();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));
        expectSuccessfulFallback(server);

        GeminiAIProvider provider = new GeminiAIProvider(restTemplate, properties, new io.micrometer.core.instrument.simple.SimpleMeterRegistry());

        assertTrue(provider.reviewCode("review").contains("\"overallScore\""));
    }

    @Test
    void geminiProviderFallsBackAfterUnauthorizedResponse() {
        AIProperties properties = enabledProperties();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        expectSuccessfulFallback(server);

        GeminiAIProvider provider = new GeminiAIProvider(restTemplate, properties, new io.micrometer.core.instrument.simple.SimpleMeterRegistry());

        assertTrue(provider.reviewCode("review").contains("\"overallScore\""));
    }

    @Test
    void geminiProviderFallsBackAfterRateLimitResponse() {
        AIProperties properties = enabledProperties();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        expectSuccessfulFallback(server);

        GeminiAIProvider provider = new GeminiAIProvider(restTemplate, properties, new io.micrometer.core.instrument.simple.SimpleMeterRegistry());

        assertTrue(provider.reviewCode("review").contains("\"overallScore\""));
    }

    @Test
    void geminiProviderFallsBackAfterForbiddenResponse() {
        AIProperties properties = enabledProperties();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));
        expectSuccessfulFallback(server);

        GeminiAIProvider provider = new GeminiAIProvider(restTemplate, properties, new io.micrometer.core.instrument.simple.SimpleMeterRegistry());

        assertTrue(provider.reviewCode("review").contains("\"overallScore\""));
    }

    @Test
    void geminiProviderFallsBackAfterTimeout() {
        AIProperties properties = enabledProperties();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withException(new SocketTimeoutException("read timed out")));
        expectSuccessfulFallback(server);

        GeminiAIProvider provider = new GeminiAIProvider(restTemplate, properties, new io.micrometer.core.instrument.simple.SimpleMeterRegistry());

        assertTrue(provider.reviewCode("review").contains("\"overallScore\""));
    }

    @Test
    void geminiProviderFallsBackAfterMalformedResponse() {
        AIProperties properties = enabledProperties();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withSuccess("not-json", MediaType.APPLICATION_JSON));
        expectSuccessfulFallback(server);

        GeminiAIProvider provider = new GeminiAIProvider(restTemplate, properties, new io.micrometer.core.instrument.simple.SimpleMeterRegistry());

        assertTrue(provider.reviewCode("review").contains("\"overallScore\""));
    }

    @Test
    void geminiProviderRejectsRequestsWhenAiIsDisabled() {
        AIProperties properties = new AIProperties();
        properties.setApiKey("test-key");

        GeminiAIProvider provider = new GeminiAIProvider(new RestTemplate(), properties, new io.micrometer.core.instrument.simple.SimpleMeterRegistry());

        assertThrows(IllegalStateException.class, () -> provider.reviewCode("review"));
    }

    private AIProperties enabledProperties() {
        AIProperties properties = new AIProperties();
        properties.setReviewEnabled(true);
        properties.setApiKey("test-key");
        properties.setBaseUrl("https://generativelanguage.googleapis.com");
        properties.setModel("gemini-2.5-flash");
        return properties;
    }

    private void expectSuccessfulFallback(MockRestServiceServer server) {
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"))
                .andRespond(withSuccess(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"{\\\"overallScore\\\":88}\"}]}}]}",
                        MediaType.APPLICATION_JSON
                ));
    }
}
