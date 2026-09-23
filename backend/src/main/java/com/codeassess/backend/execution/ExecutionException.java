package com.codeassess.backend.execution;

/**
 * Exception thrown when code execution fails at the system level.
 * This is for execution infrastructure failures, not wrong answers.
 * 
 * Examples:
 * - Judge0 API unavailable
 * - Invalid Judge0 response
 * - Network timeout
 * - Invalid language ID
 */
public class ExecutionException extends RuntimeException {

    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
