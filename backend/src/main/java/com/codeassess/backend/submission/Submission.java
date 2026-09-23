package com.codeassess.backend.submission;

import com.codeassess.backend.model.User;
import com.codeassess.backend.problem.Problem;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Represents a candidate's code submission for a problem.
 * Tracks execution status, results, and performance metrics.
 */
@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.QUEUED;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SubmissionResult result = SubmissionResult.PENDING;

    /**
     * Final score (0-100) based on test cases passed
     */
    @Column(nullable = false)
    private Integer score = 0;

    /**
     * Total execution time in milliseconds
     */
    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    /**
     * Peak memory usage in kilobytes
     */
    @Column(name = "memory_used_kb")
    private Integer memoryUsedKb;

    /**
     * Number of test cases that passed
     */
    @Column(nullable = false)
    private Integer passedTests = 0;

    /**
     * Total number of test cases executed
     */
    @Column(nullable = false)
    private Integer totalTests = 0;

    /**
     * Compiler output (for compilation errors)
     */
    @Column(columnDefinition = "TEXT")
    private String compilerOutput;

    /**
     * Runtime output or error messages
     */
    @Column(columnDefinition = "TEXT")
    private String runtimeOutput;

    /**
     * Judge0 submission token (for async tracking)
     */
    @Column(length = 100)
    private String executionToken;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime completedAt;

    public Submission() {
    }

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public User getCandidate() {
        return candidate;
    }

    public void setCandidate(User candidate) {
        this.candidate = candidate;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public SubmissionResult getResult() {
        return result;
    }

    public void setResult(SubmissionResult result) {
        this.result = result;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Integer executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public Integer getMemoryUsedKb() {
        return memoryUsedKb;
    }

    public void setMemoryUsedKb(Integer memoryUsedKb) {
        this.memoryUsedKb = memoryUsedKb;
    }

    public Integer getPassedTests() {
        return passedTests;
    }

    public void setPassedTests(Integer passedTests) {
        this.passedTests = passedTests;
    }

    public Integer getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(Integer totalTests) {
        this.totalTests = totalTests;
    }

    public String getCompilerOutput() {
        return compilerOutput;
    }

    public void setCompilerOutput(String compilerOutput) {
        this.compilerOutput = compilerOutput;
    }

    public String getRuntimeOutput() {
        return runtimeOutput;
    }

    public void setRuntimeOutput(String runtimeOutput) {
        this.runtimeOutput = runtimeOutput;
    }

    public String getExecutionToken() {
        return executionToken;
    }

    public void setExecutionToken(String executionToken) {
        this.executionToken = executionToken;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Mark the submission as completed by setting the completion timestamp.
     * Called when execution finishes (success or failure).
     */
    public void markCompleted() {
        this.completedAt = LocalDateTime.now();
    }
}
