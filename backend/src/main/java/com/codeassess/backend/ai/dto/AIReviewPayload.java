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

    private String correctedCode;
    private String explanationOfCorrection;
    private AIReviewOptimizationPayload optimizationAnalysis;
    private List<AIReviewEdgeCasePayload> generatedEdgeCases;
    private List<AIReviewFailureDiagnosisPayload> failureDiagnoses;

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

    public String getCorrectedCode() { return correctedCode; }
    public void setCorrectedCode(String correctedCode) { this.correctedCode = correctedCode; }

    public String getExplanationOfCorrection() { return explanationOfCorrection; }
    public void setExplanationOfCorrection(String explanationOfCorrection) { this.explanationOfCorrection = explanationOfCorrection; }

    public AIReviewOptimizationPayload getOptimizationAnalysis() { return optimizationAnalysis; }
    public void setOptimizationAnalysis(AIReviewOptimizationPayload optimizationAnalysis) { this.optimizationAnalysis = optimizationAnalysis; }

    public List<AIReviewEdgeCasePayload> getGeneratedEdgeCases() { return generatedEdgeCases; }
    public void setGeneratedEdgeCases(List<AIReviewEdgeCasePayload> generatedEdgeCases) { this.generatedEdgeCases = generatedEdgeCases; }

    public List<AIReviewFailureDiagnosisPayload> getFailureDiagnoses() { return failureDiagnoses; }
    public void setFailureDiagnoses(List<AIReviewFailureDiagnosisPayload> failureDiagnoses) { this.failureDiagnoses = failureDiagnoses; }
}
