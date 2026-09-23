package com.codeassess.backend.realtime;

import com.codeassess.backend.submission.Submission;

/**
 * Publishes submission state changes.
 */
public interface SubmissionEventPublisher {
    void publish(Submission submission);
    
    /**
     * Publishes submission state changes with execution context.
     * @param currentTestCase the 1-based index of the currently evaluating test case
     */
    void publish(Submission submission, Integer currentTestCase);
}
