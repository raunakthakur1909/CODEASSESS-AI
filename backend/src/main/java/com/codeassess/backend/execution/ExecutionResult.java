package com.codeassess.backend.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result from Judge0 execution for a single test case.
 * This represents the raw execution output before evaluation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {

    /**
     * Judge0 execution token for tracking
     */
    private String token;

    /**
     * Judge0 status ID
     * 1-2: In Queue/Processing
     * 3: Accepted
     * 4: Wrong Answer
     * 5: Time Limit Exceeded
     * 6: Compilation Error
     * 7-14: Various runtime errors
     */
    private Integer statusId;

    /**
     * Status description from Judge0
     */
    private String statusDescription;

    /**
     * Actual output from the program
     */
    private String stdout;

    /**
     * Error output from the program
     */
    private String stderr;

    /**
     * Compilation output (if compilation failed)
     */
    private String compileOutput;

    /**
     * Execution time in seconds
     */
    private Double time;

    /**
     * Memory used in KB
     */
    private Integer memory;

    /**
     * Whether execution completed successfully (not necessarily correct)
     */
    private boolean completed;

    /**
     * Error message if execution failed at system level
     */
    private String errorMessage;
}
