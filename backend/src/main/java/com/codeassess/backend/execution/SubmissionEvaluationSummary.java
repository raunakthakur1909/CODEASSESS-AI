package com.codeassess.backend.execution;

import com.codeassess.backend.submission.SubmissionResult;

public class SubmissionEvaluationSummary {

    private final SubmissionResult result;
    private final int passedTests;
    private final int totalTests;
    private final int score;

    public SubmissionEvaluationSummary(
            SubmissionResult result,
            int passedTests,
            int totalTests,
            int score
    ) {
        this.result = result;
        this.passedTests = passedTests;
        this.totalTests = totalTests;
        this.score = score;
    }

    public SubmissionResult getResult() {
        return result;
    }

    public int getPassedTests() {
        return passedTests;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public int getScore() {
        return score;
    }
}
