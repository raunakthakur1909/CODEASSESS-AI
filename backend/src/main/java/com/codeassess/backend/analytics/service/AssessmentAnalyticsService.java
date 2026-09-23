package com.codeassess.backend.analytics.service;

import com.codeassess.backend.analytics.dto.AssessmentAnalyticsResponse;
import com.codeassess.backend.analytics.dto.ProblemSummary;
import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.repository.AssessmentRepository;
import com.codeassess.backend.repository.ProblemRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for calculating assessment-wide analytics.
 * 
 * Aggregates submission data for an assessment to provide:
 * - Total candidates and submissions
 * - Acceptance rate and average score
 * - Per-problem breakdown
 * - Completion rate
 */
@Service
public class AssessmentAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentAnalyticsService.class);

    private final AssessmentRepository assessmentRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    public AssessmentAnalyticsService(
            AssessmentRepository assessmentRepository,
            ProblemRepository problemRepository,
            SubmissionRepository submissionRepository
    ) {
        this.assessmentRepository = assessmentRepository;
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
    }

    /**
     * Calculate comprehensive analytics for an assessment
     * 
     * Metrics include:
     * - Total unique candidates
     * - Total submissions
     * - Accepted submissions
     * - Average score and execution time
     * - Completion rate (percentage of candidates who attempted all problems)
     * - Per-problem breakdown
     */
    public AssessmentAnalyticsResponse getAssessmentAnalytics(Long assessmentId) {
        logger.debug("Calculating analytics for assessment: {}", assessmentId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        // Get total submissions and acceptance metrics
        long totalCandidates = submissionRepository.countDistinctCandidatesByAssessment(assessmentId);
        long totalSubmissions = submissionRepository.countByAssessment(assessmentId);
        long acceptedSubmissions = submissionRepository.countByAssessmentAndResult(
                assessmentId,
                SubmissionResult.ACCEPTED
        );

        // Calculate averages
        Double averageScore = submissionRepository.findAverageScoreByAssessment(assessmentId);
        Double averageExecutionTime = submissionRepository.findAverageExecutionTimeByAssessment(assessmentId);

        // Calculate completion rate
        double completionRate = calculateCompletionRate(assessmentId, totalCandidates);

        // Get per-problem breakdown
        List<Problem> problems = problemRepository.findByAssessment(assessment);
        List<ProblemSummary> problemSummaries = problems.stream()
                .map(this::getProblemSummary)
                .toList();

        AssessmentAnalyticsResponse response = new AssessmentAnalyticsResponse(
                assessmentId,
                assessment.getTitle(),
                Math.toIntExact(totalCandidates),
                Math.toIntExact(totalSubmissions),
                Math.toIntExact(acceptedSubmissions),
                averageScore != null ? Math.round(averageScore * 100.0) / 100.0 : 0.0,
                completionRate,
                averageExecutionTime != null ? Math.round(averageExecutionTime * 100.0) / 100.0 : 0.0,
                problemSummaries
        );

        logger.debug("Assessment analytics calculated: {} candidates, {} submissions, {:.2f}% acceptance",
                totalCandidates, totalSubmissions, 
                totalSubmissions > 0 ? (double) acceptedSubmissions / totalSubmissions * 100 : 0);

        return response;
    }

    /**
     * Calculate completion rate = percentage of candidates who attempted all problems
     * 
     * For now, we calculate: (successful submissions / (total candidates * total problems)) * 100
     * This approximates completion - in future could track explicit completion state
     */
    private double calculateCompletionRate(Long assessmentId, long totalCandidates) {
        if (totalCandidates == 0) {
            return 0.0;
        }

        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow();
        long totalProblems = problemRepository.countByAssessment(assessment);
        
        if (totalProblems == 0) {
            return 0.0;
        }

        long totalSubmissions = submissionRepository.countByAssessment(assessmentId);
        long acceptedSubmissions = submissionRepository.countByAssessmentAndResult(
                assessmentId,
                SubmissionResult.ACCEPTED
        );

        // Completion = accepted submissions / (candidates * problems)
        double completionRate = (double) acceptedSubmissions / (totalCandidates * totalProblems) * 100;
        return Math.min(completionRate, 100.0); // Cap at 100%
    }

    /**
     * Get summary statistics for a single problem within an assessment
     */
    private ProblemSummary getProblemSummary(Problem problem) {
        long totalSubmissions = submissionRepository.countByProblem(problem);
        long acceptedSubmissions = submissionRepository.countByProblemAndResult(
                problem,
                SubmissionResult.ACCEPTED
        );

        double acceptanceRate = totalSubmissions > 0 
                ? (double) acceptedSubmissions / totalSubmissions * 100 
                : 0.0;

        Double averageScore = submissionRepository.findAverageScoreByProblem(problem);

        return new ProblemSummary(
                problem.getId(),
                problem.getTitle(),
                Math.toIntExact(totalSubmissions),
                Math.toIntExact(acceptedSubmissions),
                Math.round(acceptanceRate * 100.0) / 100.0,
                averageScore != null ? Math.round(averageScore * 100.0) / 100.0 : 0.0
        );
    }
}
