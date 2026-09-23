package com.codeassess.backend.analytics.dto;

import java.util.List;

/**
 * Analytics response for an assessment showing aggregate statistics
 * across all candidates who have attempted the assessment.
 * 
 * Metrics include:
 * - Total number of unique candidates who submitted at least one solution
 * - Total number of submissions across all problems
 * - Number of accepted submissions (correct solutions)
 * - Average score across all submissions (0-100 scale based on test cases passed)
 * - Completion rate (percentage of candidates who attempted all problems)
 * - Average execution time in milliseconds
 * - Per-problem breakdown showing individual problem performance
 */
public class AssessmentAnalyticsResponse {

    private Long assessmentId;
    private String assessmentTitle;
    private Integer totalCandidates;
    private Integer totalSubmissions;
    private Integer acceptedSubmissions;
    private Double averageScore;
    private Double completionRate;
    private Double averageExecutionTimeMs;
    private List<ProblemSummary> problems;

    public AssessmentAnalyticsResponse() {
    }

    public AssessmentAnalyticsResponse(
            Long assessmentId,
            String assessmentTitle,
            Integer totalCandidates,
            Integer totalSubmissions,
            Integer acceptedSubmissions,
            Double averageScore,
            Double completionRate,
            Double averageExecutionTimeMs,
            List<ProblemSummary> problems
    ) {
        this.assessmentId = assessmentId;
        this.assessmentTitle = assessmentTitle;
        this.totalCandidates = totalCandidates;
        this.totalSubmissions = totalSubmissions;
        this.acceptedSubmissions = acceptedSubmissions;
        this.averageScore = averageScore;
        this.completionRate = completionRate;
        this.averageExecutionTimeMs = averageExecutionTimeMs;
        this.problems = problems;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getAssessmentTitle() {
        return assessmentTitle;
    }

    public void setAssessmentTitle(String assessmentTitle) {
        this.assessmentTitle = assessmentTitle;
    }

    public Integer getTotalCandidates() {
        return totalCandidates;
    }

    public void setTotalCandidates(Integer totalCandidates) {
        this.totalCandidates = totalCandidates;
    }

    public Integer getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(Integer totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public Integer getAcceptedSubmissions() {
        return acceptedSubmissions;
    }

    public void setAcceptedSubmissions(Integer acceptedSubmissions) {
        this.acceptedSubmissions = acceptedSubmissions;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Double getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public void setAverageExecutionTimeMs(Double averageExecutionTimeMs) {
        this.averageExecutionTimeMs = averageExecutionTimeMs;
    }

    public List<ProblemSummary> getProblems() {
        return problems;
    }

    public void setProblems(List<ProblemSummary> problems) {
        this.problems = problems;
    }
}
