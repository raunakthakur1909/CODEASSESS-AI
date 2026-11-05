package com.codeassess.backend.ai.dto;

public class AIReviewOptimizationPayload {
    private String currentApproach;
    private String optimizedApproach;
    private String currentTimeComplexity;
    private String currentSpaceComplexity;
    private String optimizedTimeComplexity;
    private String optimizedSpaceComplexity;
    private String explanation;
    private String optimizedCode;

    public String getCurrentApproach() { return currentApproach; }
    public void setCurrentApproach(String currentApproach) { this.currentApproach = currentApproach; }
    public String getOptimizedApproach() { return optimizedApproach; }
    public void setOptimizedApproach(String optimizedApproach) { this.optimizedApproach = optimizedApproach; }
    public String getCurrentTimeComplexity() { return currentTimeComplexity; }
    public void setCurrentTimeComplexity(String currentTimeComplexity) { this.currentTimeComplexity = currentTimeComplexity; }
    public String getCurrentSpaceComplexity() { return currentSpaceComplexity; }
    public void setCurrentSpaceComplexity(String currentSpaceComplexity) { this.currentSpaceComplexity = currentSpaceComplexity; }
    public String getOptimizedTimeComplexity() { return optimizedTimeComplexity; }
    public void setOptimizedTimeComplexity(String optimizedTimeComplexity) { this.optimizedTimeComplexity = optimizedTimeComplexity; }
    public String getOptimizedSpaceComplexity() { return optimizedSpaceComplexity; }
    public void setOptimizedSpaceComplexity(String optimizedSpaceComplexity) { this.optimizedSpaceComplexity = optimizedSpaceComplexity; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getOptimizedCode() { return optimizedCode; }
    public void setOptimizedCode(String optimizedCode) { this.optimizedCode = optimizedCode; }
}
