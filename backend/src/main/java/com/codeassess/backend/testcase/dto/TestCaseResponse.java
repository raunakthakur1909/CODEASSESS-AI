package com.codeassess.backend.testcase.dto;

import com.codeassess.backend.testcase.TestCase;

import java.time.LocalDateTime;

public class TestCaseResponse {

    private Long id;
    private Long problemId;
    private String input;
    private String expectedOutput;
    private Boolean hidden;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TestCaseResponse() {
    }

    public static TestCaseResponse fromEntity(
            TestCase testCase,
            boolean includeSensitiveData
    ) {

        TestCaseResponse response = new TestCaseResponse();

        response.id = testCase.getId();
        response.problemId = testCase.getProblem().getId();
        response.input = testCase.getInput();
        response.timeLimitMs = testCase.getTimeLimitMs();
        response.memoryLimitMb = testCase.getMemoryLimitMb();
        response.createdAt = testCase.getCreatedAt();
        response.updatedAt = testCase.getUpdatedAt();

        if (includeSensitiveData) {
            response.expectedOutput = testCase.getExpectedOutput();
            response.hidden = testCase.isHidden();
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public Integer getMemoryLimitMb() {
        return memoryLimitMb;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
