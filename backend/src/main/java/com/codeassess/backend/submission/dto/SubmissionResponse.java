package com.codeassess.backend.submission.dto;

import com.codeassess.backend.submission.Language;
import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.submission.SubmissionStatus;

import java.time.LocalDateTime;

/**
 * Response containing submission details and execution results.
 * Excludes sourceCode and executionToken for security.
 */
public class SubmissionResponse {

    private Long id;
    private Long problemId;
    private Long candidateId;
    private Language language;

    private SubmissionStatus status;
    private SubmissionResult result;

    private Integer score;
    private Integer passedTests;
    private Integer totalTests;

    private Integer executionTimeMs;
    private Integer memoryUsedKb;

    private String compilerOutput;
    private String runtimeOutput;

    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;

    public SubmissionResponse() {
    }

    /**
     * Convert Submission entity to response DTO.
     * Excludes sourceCode and executionToken for security.
     */
    public static SubmissionResponse fromEntity(
            Submission submission
    ) {

        SubmissionResponse response = new SubmissionResponse();

        response.id = submission.getId();

        response.problemId =
                submission.getProblem().getId();

        response.candidateId =
                submission.getCandidate().getId();

        response.language =
                submission.getLanguage();

        response.status =
                submission.getStatus();

        response.result =
                submission.getResult();

        response.score =
                submission.getScore();

        response.passedTests =
                submission.getPassedTests();

        response.totalTests =
                submission.getTotalTests();

        response.executionTimeMs =
                submission.getExecutionTimeMs();

        response.memoryUsedKb =
                submission.getMemoryUsedKb();

        response.compilerOutput =
                submission.getCompilerOutput();

        response.runtimeOutput =
                submission.getRuntimeOutput();

        response.submittedAt =
                submission.getSubmittedAt();

        response.completedAt =
                submission.getCompletedAt();

        return response;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public Language getLanguage() {
        return language;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public SubmissionResult getResult() {
        return result;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getPassedTests() {
        return passedTests;
    }

    public Integer getTotalTests() {
        return totalTests;
    }

    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }

    public Integer getMemoryUsedKb() {
        return memoryUsedKb;
    }

    public String getCompilerOutput() {
        return compilerOutput;
    }

    public String getRuntimeOutput() {
        return runtimeOutput;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
