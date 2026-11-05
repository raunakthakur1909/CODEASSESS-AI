package com.codeassess.backend.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CodeReviewOptimization {

    @Column(name = "opt_current_approach", columnDefinition = "TEXT")
    private String currentApproach;

    @Column(name = "opt_optimized_approach", columnDefinition = "TEXT")
    private String optimizedApproach;

    @Column(name = "opt_current_time", length = 50)
    private String currentTimeComplexity;

    @Column(name = "opt_current_space", length = 50)
    private String currentSpaceComplexity;

    @Column(name = "opt_optimized_time", length = 50)
    private String optimizedTimeComplexity;

    @Column(name = "opt_optimized_space", length = 50)
    private String optimizedSpaceComplexity;

    @Column(name = "opt_explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "opt_code", columnDefinition = "TEXT")
    private String optimizedCode;

    public CodeReviewOptimization() {
    }

    public String getCurrentApproach() {
        return currentApproach;
    }

    public void setCurrentApproach(String currentApproach) {
        this.currentApproach = currentApproach;
    }

    public String getOptimizedApproach() {
        return optimizedApproach;
    }

    public void setOptimizedApproach(String optimizedApproach) {
        this.optimizedApproach = optimizedApproach;
    }

    public String getCurrentTimeComplexity() {
        return currentTimeComplexity;
    }

    public void setCurrentTimeComplexity(String currentTimeComplexity) {
        this.currentTimeComplexity = currentTimeComplexity;
    }

    public String getCurrentSpaceComplexity() {
        return currentSpaceComplexity;
    }

    public void setCurrentSpaceComplexity(String currentSpaceComplexity) {
        this.currentSpaceComplexity = currentSpaceComplexity;
    }

    public String getOptimizedTimeComplexity() {
        return optimizedTimeComplexity;
    }

    public void setOptimizedTimeComplexity(String optimizedTimeComplexity) {
        this.optimizedTimeComplexity = optimizedTimeComplexity;
    }

    public String getOptimizedSpaceComplexity() {
        return optimizedSpaceComplexity;
    }

    public void setOptimizedSpaceComplexity(String optimizedSpaceComplexity) {
        this.optimizedSpaceComplexity = optimizedSpaceComplexity;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getOptimizedCode() {
        return optimizedCode;
    }

    public void setOptimizedCode(String optimizedCode) {
        this.optimizedCode = optimizedCode;
    }
}
