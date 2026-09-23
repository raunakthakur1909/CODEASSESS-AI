package com.codeassess.backend.ai;

import com.codeassess.backend.ai.dto.AIReviewPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AIReviewParserTest {

    private final AIReviewParser parser = new AIReviewParser(new ObjectMapper());

    @Test
    void shouldParseValidJson() {
        String raw = "{\n  \"overallScore\": 88,\n  \"summary\": \"Good solution\"\n}";

        AIReviewPayload payload = parser.parse(raw);

        assertEquals(88, payload.getOverallScore());
        assertEquals("Good solution", payload.getSummary());
    }

    @Test
    void shouldParseJsonInsideCodeFence() {
        String raw = "```json\n{\"overallScore\": 91, \"summary\": \"Strong review\"}\n```";

        AIReviewPayload payload = parser.parse(raw);

        assertEquals(91, payload.getOverallScore());
        assertEquals("Strong review", payload.getSummary());
    }

    @Test
    void shouldRejectMissingScore() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parse("{\"summary\": \"No score\"}"));
    }

    @Test
    void shouldRejectScoreAboveHundred() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parse("{\"overallScore\": 101, \"summary\": \"Too high\"}"));
    }

    @Test
    void shouldRejectScoreBelowZero() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parse("{\"overallScore\": -1, \"summary\": \"Too low\"}"));
    }

    @Test
    void shouldRejectMissingSummary() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parse("{\"overallScore\": 80}"));
    }

    @Test
    void shouldRejectEmptyResponse() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("   "));
    }

    @Test
    void shouldRejectMalformedJson() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parse("{\"overallScore\": 89, \"summary\": \"oops\""));
    }
}
