package com.codeassess.backend.analytics.dto;

/**
 * Analytics response for an individual candidate showing their performance
 * across all assessments and submissions.
 * 
 * Metrics include:
 * - Number of assessments the candidate has attempted
 * - Total submissions made across all assessments
 * - Number of accepted submissions (correct solutions)
 * - Number of wrong answer submissions
 * - Average score across all submissions (0-100 scale)
 * - Best score achieved in any single submission
 * - Acceptance rate (percentage of submissions that were accepted)
 * - Average execution time in milliseconds
 * - Average AI review score (if AI reviews are available)
 */
public class CandidateAnalyticsResponse {

    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private Integer assessmentsAttempted;
    private Integer totalSubmissions;
    private Integer acceptedCount;
    private Integer wrongAnswerCount;
    private Double averageScore;
    private Double bestScore;
    private Double acceptanceRate;
    private Double averageExecutionTimeMs;
    private Double averageAiReviewScore;

    public CandidateAnalyticsResponse() {
    }

    public CandidateAnalyticsResponse(
            Long candidateId,
            String candidateName,
            String candidateEmail,
            Integer assessmentsAttempted,
            Integer totalSubmissions,
            Integer acceptedCount,
            Integer wrongAnswerCount,
            Double averageScore,
            Double bestScore,
            Double acceptanceRate,
            Double averageExecutionTimeMs,
            Double averageAiReviewScore
    ) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.assessmentsAttempted = assessmentsAttempted;
        this.totalSubmissions = totalSubmissions;
        this.acceptedCount = acceptedCount;
        this.wrongAnswerCount = wrongAnswerCount;
        this.averageScore = averageScore;
        this.bestScore = bestScore;
        this.acceptanceRate = acceptanceRate;
        this.averageExecutionTimeMs = averageExecutionTimeMs;
        this.averageAiReviewScore = averageAiReviewScore;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public Integer getAssessmentsAttempted() {
        return assessmentsAttempted;
    }

    public void setAssessmentsAttempted(Integer assessmentsAttempted) {
        this.assessmentsAttempted = assessmentsAttempted;
    }

    public Integer getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(Integer totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public Integer getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(Integer acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public Integer getWrongAnswerCount() {
        return wrongAnswerCount;
    }

    public void setWrongAnswerCount(Integer wrongAnswerCount) {
        this.wrongAnswerCount = wrongAnswerCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getBestScore() {
        return bestScore;
    }

    public void setBestScore(Double bestScore) {
        this.bestScore = bestScore;
    }

    public Double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(Double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public Double getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public void setAverageExecutionTimeMs(Double averageExecutionTimeMs) {
        this.averageExecutionTimeMs = averageExecutionTimeMs;
    }

    public Double getAverageAiReviewScore() {
        return averageAiReviewScore;
    }

    public void setAverageAiReviewScore(Double averageAiReviewScore) {
        this.averageAiReviewScore = averageAiReviewScore;
    }
}
