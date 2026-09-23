package com.codeassess.backend.draft.dto;

import com.codeassess.backend.draft.Draft;
import com.codeassess.backend.submission.Language;

import java.time.LocalDateTime;

public class DraftResponse {

    private Long id;
    private Long candidateId;
    private Long assessmentId;
    private Long problemId;
    private Language language;
    private String sourceCode;
    private LocalDateTime updatedAt;

    public DraftResponse() {
    }

    public static DraftResponse fromEntity(Draft draft) {
        DraftResponse response = new DraftResponse();
        response.id = draft.getId();
        response.candidateId = draft.getCandidate().getId();
        response.assessmentId = draft.getAssessment().getId();
        response.problemId = draft.getProblem().getId();
        response.language = draft.getLanguage();
        response.sourceCode = draft.getSourceCode();
        response.updatedAt = draft.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public Language getLanguage() {
        return language;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
