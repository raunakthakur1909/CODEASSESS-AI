package com.codeassess.backend.realtime;

import java.time.Instant;

/**
 * DTO for assessment-level real-time events.
 * This is deliberately kept light and free of candidate-specific data (e.g. source code, test cases).
 */
public record AssessmentEvent(
        Long assessmentId,
        AssessmentEventType type,
        String status,
        Instant serverTime,
        Instant expiresAt
) {}
