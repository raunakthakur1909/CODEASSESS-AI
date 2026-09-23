package com.codeassess.backend.assessment.dto;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.assessment.AssessmentStatus;

import java.time.LocalDateTime;

public class AssessmentResponse {

    private Long id;
    private String title;
    private String description;
    private String instructions;
    private Integer durationMinutes;
    private Integer totalMarks;
    private Long createdBy;
    private AssessmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;

    public AssessmentResponse() {
    }

    public static AssessmentResponse fromEntity(Assessment assessment) {

        AssessmentResponse response = new AssessmentResponse();

        response.id = assessment.getId();
        response.title = assessment.getTitle();
        response.description = assessment.getDescription();
        response.instructions = assessment.getInstructions();
        response.durationMinutes = assessment.getDurationMinutes();
        response.totalMarks = assessment.getTotalMarks();
        response.createdBy = assessment.getCreatedBy().getId();
        response.status = assessment.getStatus();
        response.createdAt = assessment.getCreatedAt();
        response.updatedAt = assessment.getUpdatedAt();
        response.startedAt = assessment.getStartedAt();

        return response;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getInstructions() {
        return instructions;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }
}
