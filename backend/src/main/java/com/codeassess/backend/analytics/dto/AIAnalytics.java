package com.codeassess.backend.analytics.dto;

import java.util.List;
import java.util.Map;

/**
 * Analytics for AI code reviews showing aggregate patterns and insights.
 * This leverages the existing CodeReview and CodeReviewIssue entities
 * to provide insights into code quality patterns.
 * 
 * Metrics include:
 * - Average AI review score across all reviews (0-100 scale)
 * - Count of issues by category (SECURITY, PERFORMANCE, MAINTAINABILITY, etc.)
 * - Most common code weaknesses identified by AI
 * - Average complexity metrics (time and space complexity)
 * - Candidate improvement areas based on recurring issues
 * 
 * This is a standout feature that differentiates CodeAssess AI from
 * standard coding assessment platforms.
 */
public class AIAnalytics {

    private Integer totalReviews;
    private Double averageOverallScore;
    private Double averageTimeComplexityScore;
    private Double averageSpaceComplexityScore;
    private Integer totalIssuesFound;
    
    // Issue counts by severity
    private Integer criticalIssues;
    private Integer highIssues;
    private Integer mediumIssues;
    private Integer lowIssues;
    
    // Issue counts by category
    private Integer securityIssues;
    private Integer performanceIssues;
    private Integer maintainabilityIssues;
    private Integer complexityIssues;
    private Integer bestPracticesIssues;
    private Integer bugRiskIssues;
    
    // Most common weakness patterns (category -> count)
    private Map<String, Integer> mostCommonWeaknesses;
    
    // Candidate improvement suggestions (aggregated from reviews)
    private List<String> topImprovementAreas;

    public AIAnalytics() {
    }

    public AIAnalytics(
            Integer totalReviews,
            Double averageOverallScore,
            Double averageTimeComplexityScore,
            Double averageSpaceComplexityScore,
            Integer totalIssuesFound,
            Integer criticalIssues,
            Integer highIssues,
            Integer mediumIssues,
            Integer lowIssues,
            Integer securityIssues,
            Integer performanceIssues,
            Integer maintainabilityIssues,
            Integer complexityIssues,
            Integer bestPracticesIssues,
            Integer bugRiskIssues,
            Map<String, Integer> mostCommonWeaknesses,
            List<String> topImprovementAreas
    ) {
        this.totalReviews = totalReviews;
        this.averageOverallScore = averageOverallScore;
        this.averageTimeComplexityScore = averageTimeComplexityScore;
        this.averageSpaceComplexityScore = averageSpaceComplexityScore;
        this.totalIssuesFound = totalIssuesFound;
        this.criticalIssues = criticalIssues;
        this.highIssues = highIssues;
        this.mediumIssues = mediumIssues;
        this.lowIssues = lowIssues;
        this.securityIssues = securityIssues;
        this.performanceIssues = performanceIssues;
        this.maintainabilityIssues = maintainabilityIssues;
        this.complexityIssues = complexityIssues;
        this.bestPracticesIssues = bestPracticesIssues;
        this.bugRiskIssues = bugRiskIssues;
        this.mostCommonWeaknesses = mostCommonWeaknesses;
        this.topImprovementAreas = topImprovementAreas;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Double getAverageOverallScore() {
        return averageOverallScore;
    }

    public void setAverageOverallScore(Double averageOverallScore) {
        this.averageOverallScore = averageOverallScore;
    }

    public Double getAverageTimeComplexityScore() {
        return averageTimeComplexityScore;
    }

    public void setAverageTimeComplexityScore(Double averageTimeComplexityScore) {
        this.averageTimeComplexityScore = averageTimeComplexityScore;
    }

    public Double getAverageSpaceComplexityScore() {
        return averageSpaceComplexityScore;
    }

    public void setAverageSpaceComplexityScore(Double averageSpaceComplexityScore) {
        this.averageSpaceComplexityScore = averageSpaceComplexityScore;
    }

    public Integer getTotalIssuesFound() {
        return totalIssuesFound;
    }

    public void setTotalIssuesFound(Integer totalIssuesFound) {
        this.totalIssuesFound = totalIssuesFound;
    }

    public Integer getCriticalIssues() {
        return criticalIssues;
    }

    public void setCriticalIssues(Integer criticalIssues) {
        this.criticalIssues = criticalIssues;
    }

    public Integer getHighIssues() {
        return highIssues;
    }

    public void setHighIssues(Integer highIssues) {
        this.highIssues = highIssues;
    }

    public Integer getMediumIssues() {
        return mediumIssues;
    }

    public void setMediumIssues(Integer mediumIssues) {
        this.mediumIssues = mediumIssues;
    }

    public Integer getLowIssues() {
        return lowIssues;
    }

    public void setLowIssues(Integer lowIssues) {
        this.lowIssues = lowIssues;
    }

    public Integer getSecurityIssues() {
        return securityIssues;
    }

    public void setSecurityIssues(Integer securityIssues) {
        this.securityIssues = securityIssues;
    }

    public Integer getPerformanceIssues() {
        return performanceIssues;
    }

    public void setPerformanceIssues(Integer performanceIssues) {
        this.performanceIssues = performanceIssues;
    }

    public Integer getMaintainabilityIssues() {
        return maintainabilityIssues;
    }

    public void setMaintainabilityIssues(Integer maintainabilityIssues) {
        this.maintainabilityIssues = maintainabilityIssues;
    }

    public Integer getComplexityIssues() {
        return complexityIssues;
    }

    public void setComplexityIssues(Integer complexityIssues) {
        this.complexityIssues = complexityIssues;
    }

    public Integer getBestPracticesIssues() {
        return bestPracticesIssues;
    }

    public void setBestPracticesIssues(Integer bestPracticesIssues) {
        this.bestPracticesIssues = bestPracticesIssues;
    }

    public Integer getBugRiskIssues() {
        return bugRiskIssues;
    }

    public void setBugRiskIssues(Integer bugRiskIssues) {
        this.bugRiskIssues = bugRiskIssues;
    }

    public Map<String, Integer> getMostCommonWeaknesses() {
        return mostCommonWeaknesses;
    }

    public void setMostCommonWeaknesses(Map<String, Integer> mostCommonWeaknesses) {
        this.mostCommonWeaknesses = mostCommonWeaknesses;
    }

    public List<String> getTopImprovementAreas() {
        return topImprovementAreas;
    }

    public void setTopImprovementAreas(List<String> topImprovementAreas) {
        this.topImprovementAreas = topImprovementAreas;
    }
}
