package com.codeassess.backend.ai.dto;

import com.codeassess.backend.ai.ReviewCategory;
import com.codeassess.backend.ai.ReviewSeverity;

public class AIReviewIssuePayload {

    private ReviewSeverity severity;
    private ReviewCategory category;
    private String message;
    private String suggestion;
    private Integer lineStart;
    private Integer lineEnd;
    private String title;

    public AIReviewIssuePayload() {
    }

    public ReviewSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ReviewSeverity severity) {
        this.severity = severity;
    }

    public ReviewCategory getCategory() {
        return category;
    }

    public void setCategory(ReviewCategory category) {
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public Integer getLineStart() { return lineStart; }
    public void setLineStart(Integer lineStart) { this.lineStart = lineStart; }
    public Integer getLineEnd() { return lineEnd; }
    public void setLineEnd(Integer lineEnd) { this.lineEnd = lineEnd; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
