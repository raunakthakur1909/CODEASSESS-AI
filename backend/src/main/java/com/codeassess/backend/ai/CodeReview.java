package com.codeassess.backend.ai;

import com.codeassess.backend.submission.Submission;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "code_reviews")
public class CodeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @Column(nullable = false)
    private Integer overallScore = 0;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "complexity_time", length = 50)
    private String complexityTime;

    @Column(name = "complexity_space", length = 50)
    private String complexitySpace;

    @ElementCollection
    @CollectionTable(name = "code_review_strengths", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "strength", columnDefinition = "TEXT")
    private List<String> strengths = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "code_review_optimization_suggestions", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "suggestion", columnDefinition = "TEXT")
    private List<String> optimizationSuggestions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "code_review_security_issues", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "issue", columnDefinition = "TEXT")
    private List<String> securityIssues = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "code_review_issues", joinColumns = @JoinColumn(name = "review_id"))
    private List<CodeReviewIssue> issues = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Integer attemptCount = 0;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column
    private LocalDateTime nextAttemptAt;

    public CodeReview() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
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

    public String getComplexityTime() {
        return complexityTime;
    }

    public void setComplexityTime(String complexityTime) {
        this.complexityTime = complexityTime;
    }

    public String getComplexitySpace() {
        return complexitySpace;
    }

    public void setComplexitySpace(String complexitySpace) {
        this.complexitySpace = complexitySpace;
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

    public List<CodeReviewIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<CodeReviewIssue> issues) {
        this.issues = issues;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(LocalDateTime nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }
}
