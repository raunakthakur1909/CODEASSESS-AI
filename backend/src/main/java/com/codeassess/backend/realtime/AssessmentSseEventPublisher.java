package com.codeassess.backend.realtime;

import com.codeassess.backend.assessment.Assessment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Component
public class AssessmentSseEventPublisher implements AssessmentEventPublisher {

    private final SseConnectionManager connectionManager;

    public AssessmentSseEventPublisher(SseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void publish(Assessment assessment, AssessmentEventType type) {
        Instant expiresAt = null;
        if (assessment.getStartedAt() != null) {
            expiresAt = assessment.getStartedAt().plusMinutes(assessment.getDurationMinutes()).toInstant(ZoneOffset.UTC);
        }
        
        AssessmentEvent event = new AssessmentEvent(
                assessment.getId(),
                type,
                assessment.getStatus().name(),
                ZonedDateTime.now(ZoneOffset.UTC).toInstant(),
                expiresAt
        );

        connectionManager.publish("assessment:" + assessment.getId(), "assessment-status", event);
    }
}
