package com.codeassess.backend.realtime;

/**
 * Types of events that can be emitted on the assessment SSE channel.
 */
public enum AssessmentEventType {
    ASSESSMENT_STARTED,
    ASSESSMENT_UPDATED,
    ASSESSMENT_EXPIRING,
    ASSESSMENT_EXPIRED,
    ASSESSMENT_SUBMITTED
}
