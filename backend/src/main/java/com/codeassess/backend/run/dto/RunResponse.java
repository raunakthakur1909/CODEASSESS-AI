package com.codeassess.backend.run.dto;

import com.codeassess.backend.submission.SubmissionResult;

import java.util.List;

public class RunResponse {
    private SubmissionResult status;
    private int passedTests;
    private int totalTests;
    private String compilerOutput;
    private List<RunResult> testResults;

    public RunResponse() {}

    public SubmissionResult getStatus() { return status; }
    public void setStatus(SubmissionResult status) { this.status = status; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public String getCompilerOutput() { return compilerOutput; }
    public void setCompilerOutput(String compilerOutput) { this.compilerOutput = compilerOutput; }

    public List<RunResult> getTestResults() { return testResults; }
    public void setTestResults(List<RunResult> testResults) { this.testResults = testResults; }
}
