package com.codeassess.backend.execution;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for Judge0 API submission.
 * Maps to Judge0's submission creation endpoint.
 * 
 * Source code, stdin, and expected_output can be base64 encoded
 * by setting base64_encoded=true in the request URL.
 */
public class Judge0SubmissionRequest {

    @JsonProperty("source_code")
    private String sourceCode;

    @JsonProperty("language_id")
    private Integer languageId;

    @JsonProperty("stdin")
    private String stdin;

    @JsonProperty("expected_output")
    private String expectedOutput;

    @JsonProperty("cpu_time_limit")
    private Double cpuTimeLimit;

    @JsonProperty("memory_limit")
    private Double memoryLimit;

    @JsonProperty("wall_time_limit")
    private Double wallTimeLimit;

    public Judge0SubmissionRequest() {
    }

    public Judge0SubmissionRequest(String sourceCode, Integer languageId) {
        this.sourceCode = sourceCode;
        this.languageId = languageId;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public String getStdin() {
        return stdin;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public Double getCpuTimeLimit() {
        return cpuTimeLimit;
    }

    public void setCpuTimeLimit(Double cpuTimeLimit) {
        this.cpuTimeLimit = cpuTimeLimit;
    }

    public Double getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(Double memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public Double getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(Double wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }
}
