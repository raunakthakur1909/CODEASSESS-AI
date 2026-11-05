package com.codeassess.backend.analytics.service;

import com.codeassess.backend.analytics.dto.CandidateAnalyticsResponse;
import com.codeassess.backend.exception.ResourceNotFoundException;
import com.codeassess.backend.model.User;
import com.codeassess.backend.repository.CodeReviewRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import com.codeassess.backend.repository.UserRepository;
import com.codeassess.backend.submission.SubmissionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    private final UserRepository userRepository;

    public CandidateAnalyticsService(
            SubmissionRepository submissionRepository,
            CodeReviewRepository codeReviewRepository,
            UserRepository userRepository
    ) {
        this.submissionRepository = submissionRepository;
        this.codeReviewRepository = codeReviewRepository;
        this.userRepository = userRepository;
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

        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        String candidateName = candidate.getName() != null && !candidate.getName().isBlank()
                ? candidate.getName()
                : (candidate.getEmail() != null ? candidate.getEmail().split("@")[0] : "Candidate");
        String candidateEmail = candidate.getEmail() != null ? candidate.getEmail() : "";

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
                candidateName,
                candidateEmail,
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

        logger.debug("Candidate analytics calculated for {}: {} submissions, {:.2f}% acceptance rate",
                candidateEmail, totalSubmissions, acceptanceRate);

        return response;
    }
}
