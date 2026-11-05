package com.codeassess.backend.ai.dto;

import com.codeassess.backend.ai.CodeReview;
import com.codeassess.backend.ai.CodeReviewIssue;
import com.codeassess.backend.ai.CodeReviewEdgeCase;
import com.codeassess.backend.ai.CodeReviewFailureDiagnosis;
import com.codeassess.backend.ai.CodeReviewOptimization;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CodeReviewResponse {

    private Long id;
    private Long submissionId;

    private Integer overallScore;
    private String summary;

    private String timeComplexity;
    private String spaceComplexity;

    private List<String> strengths;
    private List<String> optimizationSuggestions;
    private List<String> securityIssues;

    private String correctedCode;
    private String explanationOfCorrection;
    private CodeReviewOptimization optimizationAnalysis;
    private List<CodeReviewEdgeCase> generatedEdgeCases;
    private List<CodeReviewFailureDiagnosis> failureDiagnoses;

    private List<CodeReviewIssueResponse> issues;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public CodeReviewResponse() {
    }

    public static CodeReviewResponse fromEntity(CodeReview review) {
        CodeReviewResponse response = new CodeReviewResponse();

        response.id = review.getId();
        response.submissionId = review.getSubmission().getId();
        response.overallScore = review.getOverallScore();
        response.summary = review.getSummary();
        response.timeComplexity = review.getComplexityTime();
        response.spaceComplexity = review.getComplexitySpace();
        response.strengths = review.getStrengths() != null ? new ArrayList<>(review.getStrengths()) : new ArrayList<>();
        response.optimizationSuggestions = review.getOptimizationSuggestions() != null ? new ArrayList<>(review.getOptimizationSuggestions()) : new ArrayList<>();
        response.securityIssues = review.getSecurityIssues() != null ? new ArrayList<>(review.getSecurityIssues()) : new ArrayList<>();
        response.status = review.getStatus().name();
        response.createdAt = review.getCreatedAt();
        response.completedAt = review.getUpdatedAt();
        response.correctedCode = review.getCorrectedCode();
        response.explanationOfCorrection = review.getExplanationOfCorrection();
        response.optimizationAnalysis = review.getOptimizationAnalysis();
        response.generatedEdgeCases = review.getGeneratedEdgeCases() != null ? new ArrayList<>(review.getGeneratedEdgeCases()) : new ArrayList<>();
        response.failureDiagnoses = review.getFailureDiagnoses() != null ? new ArrayList<>(review.getFailureDiagnoses()) : new ArrayList<>();
        response.issues = review.getIssues() != null
                ? review.getIssues().stream().map(CodeReviewResponse::toIssueResponse).toList()
                : new ArrayList<>();

        return response;
    }

    private static CodeReviewIssueResponse toIssueResponse(CodeReviewIssue issue) {
        CodeReviewIssueResponse response = new CodeReviewIssueResponse();
        response.setSeverity(issue.getSeverity());
        response.setCategory(issue.getCategory());
        response.setMessage(issue.getMessage());
        response.setSuggestion(issue.getSuggestion());
        response.setLineStart(issue.getLineStart());
        response.setLineEnd(issue.getLineEnd());
        response.setTitle(issue.getTitle());
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public String getSummary() {
        return summary;
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

    public String getSpaceComplexity() {
        return spaceComplexity;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public List<String> getOptimizationSuggestions() {
        return optimizationSuggestions;
    }

    public List<String> getSecurityIssues() {
        return securityIssues;
    }

    public List<CodeReviewIssueResponse> getIssues() {
        return issues;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getCorrectedCode() { return correctedCode; }
    public String getExplanationOfCorrection() { return explanationOfCorrection; }
    public CodeReviewOptimization getOptimizationAnalysis() { return optimizationAnalysis; }
    public List<CodeReviewEdgeCase> getGeneratedEdgeCases() { return generatedEdgeCases; }
    public List<CodeReviewFailureDiagnosis> getFailureDiagnoses() { return failureDiagnoses; }
}
