package com.codeassess.backend.ai.dto;

public class AIReviewFailureDiagnosisPayload {
    private String testCaseId;
    private String failureReason;
    private String likelyFault;
    private String explanation;
    private String suggestedFix;

    public String getTestCaseId() { return testCaseId; }
    public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getLikelyFault() { return likelyFault; }
    public void setLikelyFault(String likelyFault) { this.likelyFault = likelyFault; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getSuggestedFix() { return suggestedFix; }
    public void setSuggestedFix(String suggestedFix) { this.suggestedFix = suggestedFix; }
}
