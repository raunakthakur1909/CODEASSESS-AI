package com.codeassess.backend.analytics.service;

import com.codeassess.backend.analytics.dto.ProblemAnalyticsResponse;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.problem.Difficulty;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.repository.ProblemRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for calculating problem-level analytics.
 * 
 * Aggregates submission data for a problem to provide:
 * - Attempt counts by result (accepted, wrong answer, errors)
 * - Acceptance rate
 * - Average score and execution time
 * - Difficulty vs actual performance comparison
 */
@Service
public class ProblemAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(ProblemAnalyticsService.class);

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    public ProblemAnalyticsService(
            ProblemRepository problemRepository,
            SubmissionRepository submissionRepository
    ) {
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
    }

    /**
     * Calculate comprehensive analytics for a problem
     * 
     * Metrics include:
     * - Total attempts and results breakdown
     * - Acceptance rate
     * - Average score and execution time
     * - Performance level compared to difficulty
     */
    public ProblemAnalyticsResponse getProblemAnalytics(Long problemId) {
        logger.debug("Calculating analytics for problem: {}", problemId);

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + problemId));

        // Get result counts
        long totalAttempts = submissionRepository.countByProblem(problem);
        long acceptedCount = submissionRepository.countByProblemAndResult(problem, SubmissionResult.ACCEPTED);
        long wrongAnswerCount = submissionRepository.countByProblemAndResult(problem, SubmissionResult.WRONG_ANSWER);
        long compilationErrorCount = submissionRepository.countByProblemAndResult(
                problem,
                SubmissionResult.COMPILATION_ERROR
        );
        long runtimeErrorCount = submissionRepository.countByProblemAndResult(
                problem,
                SubmissionResult.RUNTIME_ERROR
        );

        // Calculate acceptance rate
        double acceptanceRate = totalAttempts > 0
                ? (double) acceptedCount / totalAttempts * 100
                : 0.0;

        // Get averages
        Double averageScore = submissionRepository.findAverageScoreByProblem(problem);
        Double averageExecutionTime = submissionRepository.findAverageExecutionTimeByProblem(problem);

        // Determine performance level vs difficulty
        String performanceLevel = calculatePerformanceLevel(acceptanceRate, problem.getDifficulty());

        ProblemAnalyticsResponse response = new ProblemAnalyticsResponse(
                problemId,
                problem.getTitle(),
                problem.getDifficulty().name(),
                problem.getPoints(),
                Math.toIntExact(totalAttempts),
                Math.toIntExact(acceptedCount),
                Math.toIntExact(wrongAnswerCount),
                Math.toIntExact(compilationErrorCount),
                Math.toIntExact(runtimeErrorCount),
                Math.round(acceptanceRate * 100.0) / 100.0,
                averageScore != null ? Math.round(averageScore * 100.0) / 100.0 : 0.0,
                averageExecutionTime != null ? Math.round(averageExecutionTime * 100.0) / 100.0 : 0.0,
                performanceLevel
        );

        logger.debug("Problem analytics calculated: {} attempts, {:.2f}% acceptance, performance: {}",
                totalAttempts, acceptanceRate, performanceLevel);

        return response;
    }

    /**
     * Determine if problem is easier or harder than expected
     * 
     * Expected acceptance rates by difficulty:
     * - EASY: 70-100%
     * - MEDIUM: 40-70%
     * - HARD: 10-40%
     */
    private String calculatePerformanceLevel(double acceptanceRate, Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> acceptanceRate >= 70 ? "EASIER_THAN_EXPECTED" : "AS_EXPECTED";
            case MEDIUM -> {
                if (acceptanceRate >= 70) yield "EASIER_THAN_EXPECTED";
                else if (acceptanceRate >= 40) yield "AS_EXPECTED";
                else yield "HARDER_THAN_EXPECTED";
            }
            case HARD -> acceptanceRate <= 40 ? "AS_EXPECTED" : "EASIER_THAN_EXPECTED";
        };
    }
}
