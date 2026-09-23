package com.codeassess.backend.execution;

import org.springframework.stereotype.Component;

/**
 * Evaluates test case results by comparing actual output with expected output.
 * 
 * This component:
 * - Normalizes output (line endings, trailing whitespace)
 * - Compares actual vs expected output
 * - Returns PASS/FAIL result
 * 
 * Security Note:
 * This evaluator runs backend-only and has access to hidden test case expected outputs.
 * NEVER expose expected output from hidden test cases to candidates through any API.
 */
@Component
public class TestCaseEvaluator {

    /**
     * Evaluate a single test case by comparing actual output with expected output.
     * 
     * @param actualOutput The output produced by the candidate's code
     * @param expectedOutput The expected output from the test case
     * @return TestCaseEvaluationResult with pass/fail status and both outputs
     */
    public TestCaseEvaluationResult evaluate(
            String actualOutput,
            String expectedOutput
    ) {

        String normalizedActual = normalize(actualOutput);
        String normalizedExpected = normalize(expectedOutput);

        boolean passed = normalizedActual.equals(normalizedExpected);

        return new TestCaseEvaluationResult(
                passed,
                actualOutput,
                expectedOutput
        );
    }

    /**
     * Normalize output for consistent comparison.
     * 
     * Normalization rules:
     * 1. Handle null as empty string
     * 2. Normalize line endings (Windows \r\n → Unix \n)
     * 3. Remove trailing whitespace
     * 
     * What we DON'T normalize:
     * - Internal whitespace (spaces matter: "hello world" ≠ "helloworld")
     * - Case (case-sensitive comparison)
     * - Leading whitespace in non-empty lines
     * 
     * This keeps output comparison predictable and fair.
     * 
     * @param output The output string to normalize
     * @return Normalized output
     */
    private String normalize(String output) {

        if (output == null) {
            return "";
        }

        // Normalize line endings and trim trailing whitespace
        return output
                .replace("\r\n", "\n")  // Windows → Unix
                .replace("\r", "\n")    // Old Mac → Unix
                .trim();                 // Remove trailing whitespace
    }
}
