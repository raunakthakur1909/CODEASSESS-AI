package com.codeassess.backend.analytics.service;

import com.codeassess.backend.analytics.dto.AIAnalytics;
import com.codeassess.backend.repository.CodeReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Regression tests for CodeReviewRepository analytic queries.
 *
 * <p>Background: During Milestone 10.10 E2E runs, the original JPQL queries used
 * {@code AVG(r.complexityTime)} and {@code AVG(r.complexitySpace)} against
 * {@code String}-typed fields (e.g. "O(n)", "O(1)"). This caused a Hibernate
 * type-mismatch error at runtime. The fix replaced those two queries with
 * {@code CAST(null AS double)} so that the repository always returns
 * {@code null} for complexity averages, which the service maps to 0.0.
 *
 * <p>These tests verify:
 * <ol>
 *   <li>AVG(overallScore) is correctly forwarded — the service returns the rounded value.</li>
 *   <li>Complexity queries returning {@code null} are handled gracefully (0.0 in response).</li>
 *   <li>When no reviews exist, the service short-circuits to a fully-zero response.</li>
 *   <li>Count queries are forwarded correctly.</li>
 * </ol>
 */
@DisplayName("CodeReviewRepository Analytics Regression Tests")
class CodeReviewRepositoryAnalyticsTest {

    @Mock
    private CodeReviewRepository codeReviewRepository;

    private AIAnalyticsService aiAnalyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aiAnalyticsService = new AIAnalyticsService(codeReviewRepository);
    }

    // =========================================================================
    // Assessment AI Analytics
    // =========================================================================

    @Test
    @DisplayName("getAssessmentAIAnalytics: returns zero response when no reviews exist")
    void testAssessmentAIAnalytics_NoReviews() {
        Long assessmentId = 10L;
        when(codeReviewRepository.countCompletedReviewsByAssessment(assessmentId)).thenReturn(0L);

        AIAnalytics result = aiAnalyticsService.getAssessmentAIAnalytics(assessmentId);

        assertNotNull(result);
        assertEquals(0, result.getTotalReviews());
        assertEquals(0.0, result.getAverageOverallScore());
        // Complexity scores must always be 0.0 (null queries → service fallback)
        assertEquals(0.0, result.getAverageTimeComplexityScore());
        assertEquals(0.0, result.getAverageSpaceComplexityScore());

        // Repository should not be queried beyond the count check
        verify(codeReviewRepository, never()).findAverageOverallScoreByAssessment(any());
    }

    @Test
    @DisplayName("getAssessmentAIAnalytics: correctly surfaces AVG(overallScore)")
    void testAssessmentAIAnalytics_CorrectAverageScore() {
        Long assessmentId = 11L;
        when(codeReviewRepository.countCompletedReviewsByAssessment(assessmentId)).thenReturn(3L);
        when(codeReviewRepository.findAverageOverallScoreByAssessment(assessmentId)).thenReturn(78.666);
        // Complexity queries intentionally return null (String field — cannot be averaged)
        when(codeReviewRepository.findAverageTimeComplexityByAssessment(assessmentId)).thenReturn(null);
        when(codeReviewRepository.findAverageSpaceComplexityByAssessment(assessmentId)).thenReturn(null);

        AIAnalytics result = aiAnalyticsService.getAssessmentAIAnalytics(assessmentId);

        assertEquals(3, result.getTotalReviews());
        assertEquals(78.67, result.getAverageOverallScore()); // rounded to 2dp
        // Null complexity → service must return 0.0 (not throw NPE)
        assertEquals(0.0, result.getAverageTimeComplexityScore(),
                "Complexity score must be 0.0 when repository returns null (String fields cannot be averaged)");
        assertEquals(0.0, result.getAverageSpaceComplexityScore(),
                "Space complexity score must be 0.0 when repository returns null");
    }

    @Test
    @DisplayName("getAssessmentAIAnalytics: null overallScore handled as 0.0")
    void testAssessmentAIAnalytics_NullAverageScore() {
        Long assessmentId = 12L;
        when(codeReviewRepository.countCompletedReviewsByAssessment(assessmentId)).thenReturn(1L);
        when(codeReviewRepository.findAverageOverallScoreByAssessment(assessmentId)).thenReturn(null);
        when(codeReviewRepository.findAverageTimeComplexityByAssessment(assessmentId)).thenReturn(null);
        when(codeReviewRepository.findAverageSpaceComplexityByAssessment(assessmentId)).thenReturn(null);

        AIAnalytics result = aiAnalyticsService.getAssessmentAIAnalytics(assessmentId);

        assertEquals(0.0, result.getAverageOverallScore(),
                "Null overallScore average must map to 0.0 — not throw NPE");
    }

    // =========================================================================
    // Candidate AI Analytics
    // =========================================================================

    @Test
    @DisplayName("getCandidateAIAnalytics: returns zero response when no reviews exist")
    void testCandidateAIAnalytics_NoReviews() {
        Long candidateId = 20L;
        when(codeReviewRepository.countCompletedReviewsByCandidate(candidateId)).thenReturn(0L);

        AIAnalytics result = aiAnalyticsService.getCandidateAIAnalytics(candidateId);

        assertEquals(0, result.getTotalReviews());
        assertEquals(0.0, result.getAverageOverallScore());
        assertEquals(0.0, result.getAverageTimeComplexityScore());
        assertEquals(0.0, result.getAverageSpaceComplexityScore());
        verify(codeReviewRepository, never()).findAverageOverallScoreByCandidate(any());
    }

    @Test
    @DisplayName("getCandidateAIAnalytics: correctly surfaces AVG(overallScore)")
    void testCandidateAIAnalytics_CorrectAverageScore() {
        Long candidateId = 21L;
        when(codeReviewRepository.countCompletedReviewsByCandidate(candidateId)).thenReturn(5L);
        when(codeReviewRepository.findAverageOverallScoreByCandidate(candidateId)).thenReturn(85.0);
        when(codeReviewRepository.findAverageTimeComplexityByCandidate(candidateId)).thenReturn(null);
        when(codeReviewRepository.findAverageSpaceComplexityByCandidate(candidateId)).thenReturn(null);

        AIAnalytics result = aiAnalyticsService.getCandidateAIAnalytics(candidateId);

        assertEquals(5, result.getTotalReviews());
        assertEquals(85.0, result.getAverageOverallScore());
        assertEquals(0.0, result.getAverageTimeComplexityScore(),
                "Time complexity is a String field (e.g. O(n)) — average must be null -> 0.0");
        assertEquals(0.0, result.getAverageSpaceComplexityScore());
    }

    // =========================================================================
    // Problem AI Analytics
    // =========================================================================

    @Test
    @DisplayName("getProblemAIAnalytics: returns zero response when no reviews exist")
    void testProblemAIAnalytics_NoReviews() {
        Long problemId = 30L;
        when(codeReviewRepository.countCompletedReviewsByProblem(problemId)).thenReturn(0L);

        AIAnalytics result = aiAnalyticsService.getProblemAIAnalytics(problemId);

        assertEquals(0, result.getTotalReviews());
        assertEquals(0.0, result.getAverageOverallScore());
        assertEquals(0.0, result.getAverageTimeComplexityScore());
        assertEquals(0.0, result.getAverageSpaceComplexityScore());
    }

    @Test
    @DisplayName("getProblemAIAnalytics: correctly surfaces AVG(overallScore) with rounding")
    void testProblemAIAnalytics_ScoreRounding() {
        Long problemId = 31L;
        when(codeReviewRepository.countCompletedReviewsByProblem(problemId)).thenReturn(2L);
        when(codeReviewRepository.findAverageOverallScoreByProblem(problemId)).thenReturn(92.345);
        when(codeReviewRepository.findAverageTimeComplexityByProblem(problemId)).thenReturn(null);
        when(codeReviewRepository.findAverageSpaceComplexityByProblem(problemId)).thenReturn(null);

        AIAnalytics result = aiAnalyticsService.getProblemAIAnalytics(problemId);

        assertEquals(92.35, result.getAverageOverallScore(),
                "Score should be rounded to 2 decimal places");
    }

    // =========================================================================
    // Repository count query forwarding
    // =========================================================================

    @Test
    @DisplayName("countCompletedReviewsByCandidate: correctly forwarded by service")
    void testCountByCandidate_Forwarded() {
        Long candidateId = 40L;
        when(codeReviewRepository.countCompletedReviewsByCandidate(candidateId)).thenReturn(7L);
        when(codeReviewRepository.findAverageOverallScoreByCandidate(candidateId)).thenReturn(70.0);
        when(codeReviewRepository.findAverageTimeComplexityByCandidate(candidateId)).thenReturn(null);
        when(codeReviewRepository.findAverageSpaceComplexityByCandidate(candidateId)).thenReturn(null);

        AIAnalytics result = aiAnalyticsService.getCandidateAIAnalytics(candidateId);

        assertEquals(7, result.getTotalReviews());
        verify(codeReviewRepository, times(1)).countCompletedReviewsByCandidate(candidateId);
    }
}
