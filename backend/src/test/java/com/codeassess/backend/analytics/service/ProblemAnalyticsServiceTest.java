package com.codeassess.backend.analytics.service;

import com.codeassess.backend.analytics.dto.ProblemAnalyticsResponse;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.problem.Difficulty;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.repository.ProblemRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ProblemAnalyticsService Tests")
class ProblemAnalyticsServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    private ProblemAnalyticsService problemAnalyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        problemAnalyticsService = new ProblemAnalyticsService(problemRepository, submissionRepository);
    }

    @Test
    @DisplayName("Should throw exception for non-existent problem")
    void testNonExistentProblem() {
        when(problemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> problemAnalyticsService.getProblemAnalytics(999L));
    }

    @Test
    @DisplayName("Should calculate acceptance rate correctly")
    void testAcceptanceRateCalculation() {
        Problem problem = createProblem(1L, "Sum Two Numbers", Difficulty.EASY, 100);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(submissionRepository.countByProblem(problem)).thenReturn(50L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.ACCEPTED))
                .thenReturn(40L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.WRONG_ANSWER))
                .thenReturn(8L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.COMPILATION_ERROR))
                .thenReturn(2L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.RUNTIME_ERROR))
                .thenReturn(0L);
        when(submissionRepository.findAverageScoreByProblem(problem)).thenReturn(85.0);
        when(submissionRepository.findAverageExecutionTimeByProblem(problem)).thenReturn(150.0);

        ProblemAnalyticsResponse response = problemAnalyticsService.getProblemAnalytics(1L);

        assertEquals(80.0, response.getAcceptanceRate()); // 40/50 * 100
        assertEquals(40, response.getAcceptedCount());
    }

    @Test
    @DisplayName("Should classify EASY problem with 80% acceptance as EASIER_THAN_EXPECTED")
    void testPerformanceLevelEasy() {
        Problem problem = createProblem(1L, "Easy Problem", Difficulty.EASY, 100);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(submissionRepository.countByProblem(problem)).thenReturn(10L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.ACCEPTED))
                .thenReturn(8L); // 80%
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.WRONG_ANSWER))
                .thenReturn(2L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.COMPILATION_ERROR))
                .thenReturn(0L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.RUNTIME_ERROR))
                .thenReturn(0L);
        when(submissionRepository.findAverageScoreByProblem(problem)).thenReturn(90.0);
        when(submissionRepository.findAverageExecutionTimeByProblem(problem)).thenReturn(100.0);

        ProblemAnalyticsResponse response = problemAnalyticsService.getProblemAnalytics(1L);

        assertEquals("EASIER_THAN_EXPECTED", response.getPerformanceLevel());
    }

    @Test
    @DisplayName("Should classify MEDIUM problem with 50% acceptance as AS_EXPECTED")
    void testPerformanceLevelMedium() {
        Problem problem = createProblem(1L, "Medium Problem", Difficulty.MEDIUM, 100);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(submissionRepository.countByProblem(problem)).thenReturn(10L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.ACCEPTED))
                .thenReturn(5L); // 50%
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.WRONG_ANSWER))
                .thenReturn(5L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.COMPILATION_ERROR))
                .thenReturn(0L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.RUNTIME_ERROR))
                .thenReturn(0L);
        when(submissionRepository.findAverageScoreByProblem(problem)).thenReturn(60.0);
        when(submissionRepository.findAverageExecutionTimeByProblem(problem)).thenReturn(200.0);

        ProblemAnalyticsResponse response = problemAnalyticsService.getProblemAnalytics(1L);

        assertEquals("AS_EXPECTED", response.getPerformanceLevel());
    }

    @Test
    @DisplayName("Should handle zero attempts")
    void testZeroAttempts() {
        Problem problem = createProblem(1L, "Untested Problem", Difficulty.HARD, 100);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(submissionRepository.countByProblem(problem)).thenReturn(0L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.ACCEPTED))
                .thenReturn(0L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.WRONG_ANSWER))
                .thenReturn(0L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.COMPILATION_ERROR))
                .thenReturn(0L);
        when(submissionRepository.countByProblemAndResult(problem, SubmissionResult.RUNTIME_ERROR))
                .thenReturn(0L);
        when(submissionRepository.findAverageScoreByProblem(problem)).thenReturn(null);
        when(submissionRepository.findAverageExecutionTimeByProblem(problem)).thenReturn(null);

        ProblemAnalyticsResponse response = problemAnalyticsService.getProblemAnalytics(1L);

        assertEquals(0, response.getTotalAttempts());
        assertEquals(0.0, response.getAcceptanceRate());
        assertEquals(0.0, response.getAverageScore());
    }

    private Problem createProblem(Long id, String title, Difficulty difficulty, int points) {
        Problem problem = new Problem();
        // Use reflection to set the ID since there's no setter
        try {
            java.lang.reflect.Field idField = Problem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(problem, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set Problem ID", e);
        }
        problem.setTitle(title);
        problem.setDifficulty(difficulty);
        problem.setPoints(points);
        return problem;
    }
}
