package com.codeassess.backend.realtime;

import com.codeassess.backend.submission.Submission;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SubmissionSseEventPublisher implements SubmissionEventPublisher {

    private final SseConnectionManager connectionManager;

    public SubmissionSseEventPublisher(SseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void publish(Submission submission) {
        publish(submission, null);
    }

    /*
     * The event timestamp is the moment the event is emitted, not the
     * submission time. Using submittedAt made every event in a submission's
     * lifecycle carry an identical timestamp, so clients that de-duplicate on
     * timestamp discarded every update after the first and the live status
     * never advanced past QUEUED.
     */
    @Override
    public void publish(Submission submission, Integer currentTestCase) {
        SubmissionEvent event = new SubmissionEvent(
                submission.getId(),
                submission.getStatus(),
                submission.getResult(),
                submission.getScore(),
                submission.getPassedTests(),
                submission.getTotalTests(),
                currentTestCase,
                Instant.now()
        );

        connectionManager.publish("submission:" + submission.getId(), "submission-status", event);
    }
}
