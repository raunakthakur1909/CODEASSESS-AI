package com.codeassess.backend.ai.dto;

public class AIReviewEdgeCasePayload {
    private String description;
    private String input;
    private String expectedBehavior;
    private String reason;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getExpectedBehavior() { return expectedBehavior; }
    public void setExpectedBehavior(String expectedBehavior) { this.expectedBehavior = expectedBehavior; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
