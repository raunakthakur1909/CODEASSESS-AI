package com.codeassess.backend.ai.dto;

import jakarta.validation.constraints.NotNull;

public class AIReviewRequest {

    @NotNull(message = "Submission ID is required")
    private Long submissionId;

    public AIReviewRequest() {
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }
}
