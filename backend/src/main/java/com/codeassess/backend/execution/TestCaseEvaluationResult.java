package com.codeassess.backend.execution;

/**
 * Result of evaluating a single test case.
 * Contains the comparison result and both outputs for debugging.
 */
public class TestCaseEvaluationResult {

    private final boolean passed;
    private final String actualOutput;
    private final String expectedOutput;

    public TestCaseEvaluationResult(
            boolean passed,
            String actualOutput,
            String expectedOutput
    ) {
        this.passed = passed;
        this.actualOutput = actualOutput;
        this.expectedOutput = expectedOutput;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    @Override
    public String toString() {
        return "TestCaseEvaluationResult{" +
                "passed=" + passed +
                ", actualOutput='" + actualOutput + '\'' +
                ", expectedOutput='" + expectedOutput + '\'' +
                '}';
    }
}
