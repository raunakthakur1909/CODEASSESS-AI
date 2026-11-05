package com.codeassess.backend.ai;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class CodeReviewIssue {

    @Enumerated(EnumType.STRING)
    private ReviewSeverity severity;

    @Enumerated(EnumType.STRING)
    private ReviewCategory category;

    private String message;

    private String suggestion;

    private Integer lineStart;

    private Integer lineEnd;

    private String title;

    public CodeReviewIssue() {
    }

    public CodeReviewIssue(ReviewSeverity severity, ReviewCategory category, String message, String suggestion, Integer lineStart, Integer lineEnd, String title) {
        this.severity = severity;
        this.category = category;
        this.message = message;
        this.suggestion = suggestion;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.title = title;
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

    public Integer getLineStart() {
        return lineStart;
    }

    public void setLineStart(Integer lineStart) {
        this.lineStart = lineStart;
    }

    public Integer getLineEnd() {
        return lineEnd;
    }

    public void setLineEnd(Integer lineEnd) {
        this.lineEnd = lineEnd;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
