package com.codeassess.backend.assessment.dto;

import com.codeassess.backend.assessment.AssessmentAttemptStatus;

import java.time.LocalDateTime;

public class AssessmentAttemptResponse {
    private Long id;
    private Long candidateId;
    private Long assessmentId;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private AssessmentAttemptStatus status;

    public AssessmentAttemptResponse() {}

    public AssessmentAttemptResponse(Long id, Long candidateId, Long assessmentId, LocalDateTime startedAt, LocalDateTime expiresAt, AssessmentAttemptStatus status) {
        this.id = id;
        this.candidateId = candidateId;
        this.assessmentId = assessmentId;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public AssessmentAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentAttemptStatus status) {
        this.status = status;
    }
}
