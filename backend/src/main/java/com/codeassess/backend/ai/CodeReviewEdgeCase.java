package com.codeassess.backend.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CodeReviewEdgeCase {

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String expectedBehavior;

    @Column(columnDefinition = "TEXT")
    private String reason;

    public CodeReviewEdgeCase() {
    }

    public CodeReviewEdgeCase(String description, String input, String expectedBehavior, String reason) {
        this.description = description;
        this.input = input;
        this.expectedBehavior = expectedBehavior;
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedBehavior() {
        return expectedBehavior;
    }

    public void setExpectedBehavior(String expectedBehavior) {
        this.expectedBehavior = expectedBehavior;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
