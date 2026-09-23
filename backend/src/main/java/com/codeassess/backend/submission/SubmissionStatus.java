package com.codeassess.backend.submission;

/**
 * Execution status of a code submission.
 * Tracks the lifecycle from queued through execution to completion.
 */
public enum SubmissionStatus {
    
    /**
     * Submission received and waiting for execution
     */
    QUEUED,
    
    /**
     * Currently being executed by the code runner
     */
    RUNNING,
    
    /**
     * Test cases are currently being evaluated sequentially
     */
    EVALUATING,
    
    /**
     * Execution completed (check result field for evaluation outcome)
     */
    COMPLETED,
    
    /**
     * System error during execution (not a candidate code error)
     */
    FAILED
}
