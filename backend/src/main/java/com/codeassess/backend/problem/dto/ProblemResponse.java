package com.codeassess.backend.problem.dto;

import com.codeassess.backend.problem.Difficulty;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.submission.Language;

import java.time.LocalDateTime;

public class ProblemResponse {

    private Long id;
    private Long assessmentId;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Integer points;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private String starterCode;
    private Language starterLanguage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProblemResponse() {
    }

    public static ProblemResponse fromEntity(Problem problem) {

        ProblemResponse response = new ProblemResponse();

        response.id = problem.getId();
        response.assessmentId = problem.getAssessment().getId();
        response.title = problem.getTitle();
        response.description = problem.getDescription();
        response.difficulty = problem.getDifficulty();
        response.points = problem.getPoints();
        response.inputFormat = problem.getInputFormat();
        response.outputFormat = problem.getOutputFormat();
        response.constraints = problem.getConstraints();
        response.starterCode = problem.getStarterCode();
        response.starterLanguage = problem.getStarterLanguage();
        response.createdAt = problem.getCreatedAt();
        response.updatedAt = problem.getUpdatedAt();

        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Integer getPoints() {
        return points;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getConstraints() {
        return constraints;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public Language getStarterLanguage() {
        return starterLanguage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
