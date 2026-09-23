package com.codeassess.backend.submission;

/**
 * Evaluation result of a code submission.
 * Separate from execution status to distinguish system state from code evaluation.
 */
public enum SubmissionResult {
    
    /**
     * All test cases passed
     */
    ACCEPTED,
    
    /**
     * Code executed but produced incorrect output
     */
    WRONG_ANSWER,
    
    /**
     * Code failed to compile
     */
    COMPILATION_ERROR,
    
    /**
     * Runtime error during execution (e.g., exception, segfault)
     */
    RUNTIME_ERROR,
    
    /**
     * Execution exceeded time limit
     */
    TIME_LIMIT_EXCEEDED,
    
    /**
     * Execution exceeded memory limit
     */
    MEMORY_LIMIT_EXCEEDED,
    
    /**
     * System error (not candidate's fault)
     */
    SYSTEM_ERROR,
    
    /**
     * Not yet evaluated (submission still queued or running)
     */
    PENDING
}
