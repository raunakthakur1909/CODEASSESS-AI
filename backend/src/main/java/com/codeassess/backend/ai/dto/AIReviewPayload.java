package com.codeassess.backend.ai.dto;

import java.util.List;

public class AIReviewPayload {

    private Integer overallScore;
    private String summary;

    private String timeComplexity;
    private String spaceComplexity;

    private List<String> strengths;
    private List<String> optimizationSuggestions;
    private List<String> securityIssues;

    private List<AIReviewIssuePayload> issues;

    public AIReviewPayload() {
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

    public void setTimeComplexity(String timeComplexity) {
        this.timeComplexity = timeComplexity;
    }

    public String getSpaceComplexity() {
        return spaceComplexity;
    }

    public void setSpaceComplexity(String spaceComplexity) {
        this.spaceComplexity = spaceComplexity;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getOptimizationSuggestions() {
        return optimizationSuggestions;
    }

    public void setOptimizationSuggestions(List<String> optimizationSuggestions) {
        this.optimizationSuggestions = optimizationSuggestions;
    }

    public List<String> getSecurityIssues() {
        return securityIssues;
    }

    public void setSecurityIssues(List<String> securityIssues) {
        this.securityIssues = securityIssues;
    }

    public List<AIReviewIssuePayload> getIssues() {
        return issues;
    }

    public void setIssues(List<AIReviewIssuePayload> issues) {
        this.issues = issues;
    }
}
