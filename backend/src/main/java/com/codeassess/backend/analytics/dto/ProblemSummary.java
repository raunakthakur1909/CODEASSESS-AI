package com.codeassess.backend.analytics.dto;

/**
 * Summary statistics for a single problem within an assessment.
 * Used as part of assessment analytics to show performance breakdown by problem.
 */
public class ProblemSummary {

    private Long problemId;
    private String title;
    private Integer totalSubmissions;
    private Integer acceptedSubmissions;
    private Double acceptanceRate;
    private Double averageScore;

    public ProblemSummary() {
    }

    public ProblemSummary(
            Long problemId,
            String title,
            Integer totalSubmissions,
            Integer acceptedSubmissions,
            Double acceptanceRate,
            Double averageScore
    ) {
        this.problemId = problemId;
        this.title = title;
        this.totalSubmissions = totalSubmissions;
        this.acceptedSubmissions = acceptedSubmissions;
        this.acceptanceRate = acceptanceRate;
        this.averageScore = averageScore;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(Double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }
}
