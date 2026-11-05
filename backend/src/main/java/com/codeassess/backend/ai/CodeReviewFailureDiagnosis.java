package com.codeassess.backend.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CodeReviewFailureDiagnosis {

    @Column(length = 100)
    private String testCaseId;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(columnDefinition = "TEXT")
    private String likelyFault;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String suggestedFix;

    public CodeReviewFailureDiagnosis() {
    }

    public CodeReviewFailureDiagnosis(String testCaseId, String failureReason, String likelyFault, String explanation, String suggestedFix) {
        this.testCaseId = testCaseId;
        this.failureReason = failureReason;
        this.likelyFault = likelyFault;
        this.explanation = explanation;
        this.suggestedFix = suggestedFix;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getLikelyFault() {
        return likelyFault;
    }

    public void setLikelyFault(String likelyFault) {
        this.likelyFault = likelyFault;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getSuggestedFix() {
        return suggestedFix;
    }

    public void setSuggestedFix(String suggestedFix) {
        this.suggestedFix = suggestedFix;
    }
}
