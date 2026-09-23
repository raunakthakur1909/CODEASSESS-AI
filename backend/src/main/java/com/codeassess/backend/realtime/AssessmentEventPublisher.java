package com.codeassess.backend.realtime;

import com.codeassess.backend.assessment.Assessment;

public interface AssessmentEventPublisher {
    
    /**
     * Publishes an assessment event.
     */
    void publish(Assessment assessment, AssessmentEventType type);
}
