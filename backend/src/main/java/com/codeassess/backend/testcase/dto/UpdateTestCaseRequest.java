package com.codeassess.backend.testcase.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateTestCaseRequest {

    @NotBlank(message = "Input is required")
    private String input;

    @NotBlank(message = "Expected output is required")
    private String expectedOutput;

    @NotNull(message = "Hidden flag is required")
    private Boolean hidden;

    @NotNull(message = "Time limit is required")
    @Min(value = 100, message = "Time limit must be at least 100 ms")
    @Max(value = 30000, message = "Time limit cannot exceed 30000 ms")
    private Integer timeLimitMs;

    @NotNull(message = "Memory limit is required")
    @Min(value = 16, message = "Memory limit must be at least 16 MB")
    @Max(value = 2048, message = "Memory limit cannot exceed 2048 MB")
    private Integer memoryLimitMb;

    public UpdateTestCaseRequest() {
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public void setTimeLimitMs(Integer timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }

    public Integer getMemoryLimitMb() {
        return memoryLimitMb;
    }

    public void setMemoryLimitMb(Integer memoryLimitMb) {
        this.memoryLimitMb = memoryLimitMb;
    }
}
