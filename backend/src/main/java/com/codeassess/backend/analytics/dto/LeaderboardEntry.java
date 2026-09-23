package com.codeassess.backend.analytics.dto;

import java.time.LocalDateTime;

/**
 * Represents a single entry in an assessment leaderboard.
 * Candidates are ranked by:
 * 1. Total score (descending)
 * 2. Number of accepted submissions (descending)
 * 3. Average execution time (ascending - faster is better)
 * 4. Submission timestamp (ascending - earlier is better for ties)
 * 
 * This provides deterministic ranking even when candidates have identical scores.
 */
public class LeaderboardEntry {

    private Integer rank;
    private Long candidateId;
    private String candidateName;
    private Double totalScore;
    private Integer acceptedCount;
    private Integer totalProblems;
    private Double averageExecutionTimeMs;
    private LocalDateTime lastSubmissionAt;

    public LeaderboardEntry() {
    }

    public LeaderboardEntry(
            Integer rank,
            Long candidateId,
            String candidateName,
            Double totalScore,
            Integer acceptedCount,
            Integer totalProblems,
            Double averageExecutionTimeMs,
            LocalDateTime lastSubmissionAt
    ) {
        this.rank = rank;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.totalScore = totalScore;
        this.acceptedCount = acceptedCount;
        this.totalProblems = totalProblems;
        this.averageExecutionTimeMs = averageExecutionTimeMs;
        this.lastSubmissionAt = lastSubmissionAt;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
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

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(Integer acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public Integer getTotalProblems() {
        return totalProblems;
    }

    public void setTotalProblems(Integer totalProblems) {
        this.totalProblems = totalProblems;
    }

    public Double getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public void setAverageExecutionTimeMs(Double averageExecutionTimeMs) {
        this.averageExecutionTimeMs = averageExecutionTimeMs;
    }

    public LocalDateTime getLastSubmissionAt() {
        return lastSubmissionAt;
    }

    public void setLastSubmissionAt(LocalDateTime lastSubmissionAt) {
        this.lastSubmissionAt = lastSubmissionAt;
    }
}
