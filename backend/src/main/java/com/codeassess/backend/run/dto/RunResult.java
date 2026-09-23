package com.codeassess.backend.run.dto;

public class RunResult {
    private Long testCaseId;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private boolean passed;
    private String errorMessage;
    private Double time;
    private Integer memory;

    public RunResult() {}

    public Long getTestCaseId() { return testCaseId; }
    public void setTestCaseId(Long testCaseId) { this.testCaseId = testCaseId; }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public String getActualOutput() { return actualOutput; }
    public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Double getTime() { return time; }
    public void setTime(Double time) { this.time = time; }

    public Integer getMemory() { return memory; }
    public void setMemory(Integer memory) { this.memory = memory; }
}
