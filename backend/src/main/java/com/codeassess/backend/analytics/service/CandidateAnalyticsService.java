package com.codeassess.backend.analytics.service;

import com.codeassess.backend.analytics.dto.CandidateAnalyticsResponse;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.model.User;
import com.codeassess.backend.repository.CodeReviewRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for calculating individual candidate analytics.
 * 
 * Aggregates submission data for a candidate to provide:
 * - Assessments attempted
 * - Total submissions and acceptance rate
 * - Average and best scores
 * - AI code quality insights
 */
@Service
public class CandidateAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(CandidateAnalyticsService.class);

    private final SubmissionRepository submissionRepository;
    private final CodeReviewRepository codeReviewRepository;

    public CandidateAnalyticsService(
            SubmissionRepository submissionRepository,
                            CodeReviewRepository codeReviewRepository
    ) {
        this.submissionRepository = submissionRepository;
        this.codeReviewRepository = codeReviewRepository;
    }

    /**
     * Calculate comprehensive analytics for a candidate
     * 
     * Metrics include:
     * - Assessments attempted
     * - Total submissions, accepted, and wrong answers
     * - Average and best scores
     * - Acceptance rate
     * - Average execution time
     * - Average AI review score
     */
    public CandidateAnalyticsResponse getCandidateAnalytics(Long candidateId) {
        logger.debug("Calculating analytics for candidate: {}", candidateId);

        // Create a minimal User object for repository queries
        User candidate = new User();
        candidate.setId(candidateId);

        // Submission metrics
        long assessmentsAttempted = submissionRepository.countDistinctAssessmentsByCandidate(candidate);
        long totalSubmissions = submissionRepository.countByCandidate(candidate);
        long acceptedCount = submissionRepository.countByCandidateAndResult(candidate, SubmissionResult.ACCEPTED);
        long wrongAnswerCount = submissionRepository.countByCandidateAndResult(candidate, SubmissionResult.WRONG_ANSWER);

        Double averageScore = submissionRepository.findAverageScoreByCandidate(candidate);
        Double bestScore = submissionRepository.findMaxScoreByCandidate(candidate);

        // Calculate acceptance rate
        double acceptanceRate = totalSubmissions > 0
                ? (double) acceptedCount / totalSubmissions * 100
                : 0.0;

        Double averageExecutionTime = submissionRepository.findAverageExecutionTimeByCandidate(candidate);

        // AI Review metrics
        Double averageAiReviewScore = codeReviewRepository.findAverageOverallScoreByCandidate(candidateId);

        CandidateAnalyticsResponse response = new CandidateAnalyticsResponse(
                candidateId,
                "Unknown", // Name would need to be fetched from UserRepository if needed
                "unknown@example.com", // Email would need to be fetched from UserRepository if needed
                Math.toIntExact(assessmentsAttempted),
                Math.toIntExact(totalSubmissions),
                Math.toIntExact(acceptedCount),
                Math.toIntExact(wrongAnswerCount),
                averageScore != null ? Math.round(averageScore * 100.0) / 100.0 : 0.0,
                bestScore != null ? Math.round(bestScore * 100.0) / 100.0 : 0.0,
                Math.round(acceptanceRate * 100.0) / 100.0,
                averageExecutionTime != null ? Math.round(averageExecutionTime * 100.0) / 100.0 : 0.0,
                averageAiReviewScore != null ? Math.round(averageAiReviewScore * 100.0) / 100.0 : null
        );

        logger.debug("Candidate analytics calculated: {} submissions, {:.2f}% acceptance rate",
                totalSubmissions, acceptanceRate);

        return response;
    }
}
