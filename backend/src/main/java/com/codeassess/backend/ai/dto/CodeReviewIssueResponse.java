package com.codeassess.backend.ai.dto;

import com.codeassess.backend.ai.ReviewCategory;
import com.codeassess.backend.ai.ReviewSeverity;

public class CodeReviewIssueResponse {

    private ReviewSeverity severity;
    private ReviewCategory category;
    private String message;
    private String suggestion;

    public CodeReviewIssueResponse() {
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
}
