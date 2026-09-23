package com.codeassess.backend.analytics.dto;

/**
 * Analytics response for a specific problem showing aggregate statistics
 * across all candidates who have attempted it.
 * 
 * Metrics include:
 * - Total number of submission attempts
 * - Number of accepted submissions (correct solutions)
 * - Number of wrong answer submissions
 * - Number of compilation error submissions
 * - Acceptance rate (percentage of submissions that were accepted)
 * - Average score across all submissions (0-100 scale)
 * - Average execution time in milliseconds
 * - Difficulty vs actual performance comparison (how hard it proved vs stated difficulty)
 */
public class ProblemAnalyticsResponse {

    private Long problemId;
    private String title;
    private String difficulty;
    private Integer totalPoints;
    private Integer totalAttempts;
    private Integer acceptedCount;
    private Integer wrongAnswerCount;
    private Integer compilationErrorCount;
    private Integer runtimeErrorCount;
    private Double acceptanceRate;
    private Double averageScore;
    private Double averageExecutionTimeMs;
    private String performanceLevel; // "EASIER_THAN_EXPECTED", "AS_EXPECTED", "HARDER_THAN_EXPECTED"

    public ProblemAnalyticsResponse() {
    }

    public ProblemAnalyticsResponse(
            Long problemId,
            String title,
            String difficulty,
            Integer totalPoints,
            Integer totalAttempts,
            Integer acceptedCount,
            Integer wrongAnswerCount,
            Integer compilationErrorCount,
            Integer runtimeErrorCount,
            Double acceptanceRate,
            Double averageScore,
            Double averageExecutionTimeMs,
            String performanceLevel
    ) {
        this.problemId = problemId;
        this.title = title;
        this.difficulty = difficulty;
        this.totalPoints = totalPoints;
        this.totalAttempts = totalAttempts;
        this.acceptedCount = acceptedCount;
        this.wrongAnswerCount = wrongAnswerCount;
        this.compilationErrorCount = compilationErrorCount;
        this.runtimeErrorCount = runtimeErrorCount;
        this.acceptanceRate = acceptanceRate;
        this.averageScore = averageScore;
        this.averageExecutionTimeMs = averageExecutionTimeMs;
        this.performanceLevel = performanceLevel;
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

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(Integer totalAttempts) {
        this.totalAttempts = totalAttempts;
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

    public Integer getCompilationErrorCount() {
        return compilationErrorCount;
    }

    public void setCompilationErrorCount(Integer compilationErrorCount) {
        this.compilationErrorCount = compilationErrorCount;
    }

    public Integer getRuntimeErrorCount() {
        return runtimeErrorCount;
    }

    public void setRuntimeErrorCount(Integer runtimeErrorCount) {
        this.runtimeErrorCount = runtimeErrorCount;
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

    public Double getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public void setAverageExecutionTimeMs(Double averageExecutionTimeMs) {
        this.averageExecutionTimeMs = averageExecutionTimeMs;
    }

    public String getPerformanceLevel() {
        return performanceLevel;
    }

    public void setPerformanceLevel(String performanceLevel) {
        this.performanceLevel = performanceLevel;
    }
}
