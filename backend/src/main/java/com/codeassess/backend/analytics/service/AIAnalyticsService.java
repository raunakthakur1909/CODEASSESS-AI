package com.codeassess.backend.analytics.service;

import com.codeassess.backend.analytics.dto.AIAnalytics;
import com.codeassess.backend.repository.CodeReviewRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalyticsService.class);

    private final CodeReviewRepository codeReviewRepository;

    public AIAnalyticsService(
            CodeReviewRepository codeReviewRepository
    ) {
        this.codeReviewRepository = codeReviewRepository;
    }

    public AIAnalytics getAssessmentAIAnalytics(Long assessmentId) {
        logger.debug("Calculating AI analytics for assessment: {}", assessmentId);

        long totalReviews = codeReviewRepository.countCompletedReviewsByAssessment(assessmentId);
        
        if (totalReviews == 0) {
            logger.debug("No completed AI reviews found for assessment");
            return createEmptyAIAnalytics();
        }

        Double averageOverallScore = codeReviewRepository.findAverageOverallScoreByAssessment(assessmentId);
        Double averageTimeComplexity = codeReviewRepository.findAverageTimeComplexityByAssessment(assessmentId);
        Double averageSpaceComplexity = codeReviewRepository.findAverageSpaceComplexityByAssessment(assessmentId);

        Map<String, Integer> issueCounts = aggregateCategoryCounts(
                codeReviewRepository.countCompletedIssueCategoriesByAssessment(assessmentId)
        );
        Map<String, Integer> severityCounts = aggregateSeverityCounts(
                codeReviewRepository.countCompletedIssueSeveritiesByAssessment(assessmentId)
        );
        List<String> topImprovementAreas = deriveTopImprovementAreas(issueCounts);

        return new AIAnalytics(
                Math.toIntExact(totalReviews),
                averageOverallScore != null ? Math.round(averageOverallScore * 100.0) / 100.0 : 0.0,
                averageTimeComplexity != null ? Math.round(averageTimeComplexity * 100.0) / 100.0 : 0.0,
                averageSpaceComplexity != null ? Math.round(averageSpaceComplexity * 100.0) / 100.0 : 0.0,
                issueCounts.values().stream().mapToInt(Integer::intValue).sum(),
                severityCounts.getOrDefault("CRITICAL", 0),
                severityCounts.getOrDefault("HIGH", 0),
                severityCounts.getOrDefault("MEDIUM", 0),
                severityCounts.getOrDefault("LOW", 0),
                issueCounts.getOrDefault("SECURITY", 0),
                issueCounts.getOrDefault("PERFORMANCE", 0),
                issueCounts.getOrDefault("MAINTAINABILITY", 0),
                issueCounts.getOrDefault("COMPLEXITY", 0),
                issueCounts.getOrDefault("BEST_PRACTICE", 0),
                issueCounts.getOrDefault("BUG_RISK", 0),
                issueCounts,
                topImprovementAreas
        );
    }

    public AIAnalytics getCandidateAIAnalytics(Long candidateId) {
        logger.debug("Calculating AI analytics for candidate: {}", candidateId);

        long totalReviews = codeReviewRepository.countCompletedReviewsByCandidate(candidateId);
        
        if (totalReviews == 0) {
            logger.debug("No completed AI reviews found for candidate");
            return createEmptyAIAnalytics();
        }

        Double averageOverallScore = codeReviewRepository.findAverageOverallScoreByCandidate(candidateId);
        Double averageTimeComplexity = codeReviewRepository.findAverageTimeComplexityByCandidate(candidateId);
        Double averageSpaceComplexity = codeReviewRepository.findAverageSpaceComplexityByCandidate(candidateId);

        Map<String, Integer> issueCounts = aggregateCategoryCounts(
                codeReviewRepository.countCompletedIssueCategoriesByCandidate(candidateId)
        );
        Map<String, Integer> severityCounts = aggregateSeverityCounts(
                codeReviewRepository.countCompletedIssueSeveritiesByCandidate(candidateId)
        );
        List<String> topImprovementAreas = deriveTopImprovementAreas(issueCounts);

        return new AIAnalytics(
                Math.toIntExact(totalReviews),
                averageOverallScore != null ? Math.round(averageOverallScore * 100.0) / 100.0 : 0.0,
                averageTimeComplexity != null ? Math.round(averageTimeComplexity * 100.0) / 100.0 : 0.0,
                averageSpaceComplexity != null ? Math.round(averageSpaceComplexity * 100.0) / 100.0 : 0.0,
                issueCounts.values().stream().mapToInt(Integer::intValue).sum(),
                severityCounts.getOrDefault("CRITICAL", 0),
                severityCounts.getOrDefault("HIGH", 0),
                severityCounts.getOrDefault("MEDIUM", 0),
                severityCounts.getOrDefault("LOW", 0),
                issueCounts.getOrDefault("SECURITY", 0),
                issueCounts.getOrDefault("PERFORMANCE", 0),
                issueCounts.getOrDefault("MAINTAINABILITY", 0),
                issueCounts.getOrDefault("COMPLEXITY", 0),
                issueCounts.getOrDefault("BEST_PRACTICE", 0),
                issueCounts.getOrDefault("BUG_RISK", 0),
                issueCounts,
                topImprovementAreas
        );
    }

    public AIAnalytics getProblemAIAnalytics(Long problemId) {
        logger.debug("Calculating AI analytics for problem: {}", problemId);

        long totalReviews = codeReviewRepository.countCompletedReviewsByProblem(problemId);
        
        if (totalReviews == 0) {
            logger.debug("No completed AI reviews found for problem");
            return createEmptyAIAnalytics();
        }

        Double averageOverallScore = codeReviewRepository.findAverageOverallScoreByProblem(problemId);
        Double averageTimeComplexity = codeReviewRepository.findAverageTimeComplexityByProblem(problemId);
        Double averageSpaceComplexity = codeReviewRepository.findAverageSpaceComplexityByProblem(problemId);

        Map<String, Integer> issueCounts = aggregateCategoryCounts(
                codeReviewRepository.countCompletedIssueCategoriesByProblem(problemId)
        );
        Map<String, Integer> severityCounts = aggregateSeverityCounts(
                codeReviewRepository.countCompletedIssueSeveritiesByProblem(problemId)
        );
        List<String> topImprovementAreas = deriveTopImprovementAreas(issueCounts);

        return new AIAnalytics(
                Math.toIntExact(totalReviews),
                averageOverallScore != null ? Math.round(averageOverallScore * 100.0) / 100.0 : 0.0,
                averageTimeComplexity != null ? Math.round(averageTimeComplexity * 100.0) / 100.0 : 0.0,
                averageSpaceComplexity != null ? Math.round(averageSpaceComplexity * 100.0) / 100.0 : 0.0,
                issueCounts.values().stream().mapToInt(Integer::intValue).sum(),
                severityCounts.getOrDefault("CRITICAL", 0),
                severityCounts.getOrDefault("HIGH", 0),
                severityCounts.getOrDefault("MEDIUM", 0),
                severityCounts.getOrDefault("LOW", 0),
                issueCounts.getOrDefault("SECURITY", 0),
                issueCounts.getOrDefault("PERFORMANCE", 0),
                issueCounts.getOrDefault("MAINTAINABILITY", 0),
                issueCounts.getOrDefault("COMPLEXITY", 0),
                issueCounts.getOrDefault("BEST_PRACTICE", 0),
                issueCounts.getOrDefault("BUG_RISK", 0),
                issueCounts,
                topImprovementAreas
        );
    }

    private Map<String, Integer> aggregateSeverityCounts(List<Object[]> rows) {
        Map<String, Integer> counts = new HashMap<>();
        if (rows == null) return counts;
        for (Object[] row : rows) {
            String severity = (String) row[0];
            Long count = (Long) row[1];
            counts.put(severity, count.intValue());
        }
        return counts;
    }

    private Map<String, Integer> aggregateCategoryCounts(List<Object[]> rows) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        if (rows == null) return counts;
        for (Object[] row : rows) {
            String category = (String) row[0];
            Long count = (Long) row[1];
            counts.put(category, count.intValue());
        }
        return counts;
    }

    private List<String> deriveTopImprovementAreas(Map<String, Integer> issueCounts) {
        return issueCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    private AIAnalytics createEmptyAIAnalytics() {
        return new AIAnalytics(
                0, // totalReviews
                0.0, // averageOverallScore
                0.0, // averageTimeComplexityScore
                0.0, // averageSpaceComplexityScore
                0, // totalIssuesFound
                0, 0, 0, 0, // severity counts
                0, 0, 0, 0, 0, 0, // category counts
                new HashMap<>(),
                List.of()
        );
    }
}
