package com.codeassess.backend.analytics.service;

import com.codeassess.backend.analytics.dto.CandidateAnalyticsResponse;
import com.codeassess.backend.model.Role;
import com.codeassess.backend.model.User;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.repository.CodeReviewRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CandidateAnalyticsService Tests")
class CandidateAnalyticsServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private CodeReviewRepository codeReviewRepository;

    @Mock
    private com.codeassess.backend.repository.UserRepository userRepository;

    private CandidateAnalyticsService candidateAnalyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        candidateAnalyticsService = new CandidateAnalyticsService(submissionRepository, codeReviewRepository, userRepository);
    }

    @Test
    @DisplayName("Should calculate correct acceptance rate")
    void testAcceptanceRateCalculation() {
        Long candidateId = 1L;
        User candidate = createUser(candidateId, "John Doe");

        when(userRepository.findById(candidateId)).thenReturn(java.util.Optional.of(candidate));
        when(submissionRepository.countDistinctAssessmentsByCandidate(any(User.class))).thenReturn(2L);
        when(submissionRepository.countByCandidate(any(User.class))).thenReturn(10L);
        when(submissionRepository.countByCandidateAndResult(any(User.class), eq(SubmissionResult.ACCEPTED)))
                .thenReturn(7L);
        when(submissionRepository.countByCandidateAndResult(any(User.class), eq(SubmissionResult.WRONG_ANSWER)))
                .thenReturn(3L);
        when(submissionRepository.findAverageScoreByCandidate(any(User.class))).thenReturn(75.0);
        when(submissionRepository.findMaxScoreByCandidate(any(User.class))).thenReturn(100.0);
        when(submissionRepository.findAverageExecutionTimeByCandidate(any(User.class))).thenReturn(250.0);
        when(codeReviewRepository.findAverageOverallScoreByCandidate(candidateId)).thenReturn(80.0);

        CandidateAnalyticsResponse response = candidateAnalyticsService.getCandidateAnalytics(candidateId);

        assertEquals(70.0, response.getAcceptanceRate()); // 7/10 * 100 = 70%
        assertEquals("John Doe", response.getCandidateName());
        assertEquals("user1@example.com", response.getCandidateEmail());
    }

    @Test
    @DisplayName("Should handle zero submissions")
    void testZeroSubmissions() {
        Long candidateId = 1L;
        User candidate = createUser(candidateId, "Jane Doe");

        when(userRepository.findById(candidateId)).thenReturn(java.util.Optional.of(candidate));
        when(submissionRepository.countDistinctAssessmentsByCandidate(any(User.class))).thenReturn(0L);
        when(submissionRepository.countByCandidate(any(User.class))).thenReturn(0L);
        when(submissionRepository.countByCandidateAndResult(any(User.class), eq(SubmissionResult.ACCEPTED)))
                .thenReturn(0L);
        when(submissionRepository.countByCandidateAndResult(any(User.class), eq(SubmissionResult.WRONG_ANSWER)))
                .thenReturn(0L);
        when(submissionRepository.findAverageScoreByCandidate(any(User.class))).thenReturn(null);
        when(submissionRepository.findMaxScoreByCandidate(any(User.class))).thenReturn(null);
        when(submissionRepository.findAverageExecutionTimeByCandidate(any(User.class))).thenReturn(null);
        when(codeReviewRepository.findAverageOverallScoreByCandidate(candidateId)).thenReturn(null);

        CandidateAnalyticsResponse response = candidateAnalyticsService.getCandidateAnalytics(candidateId);

        assertEquals(0, response.getTotalSubmissions());
        assertEquals(0.0, response.getAcceptanceRate());
        assertEquals(0.0, response.getAverageScore());
    }

    @Test
    @DisplayName("Should populate all fields correctly")
    void testAllFieldsPopulated() {
        Long candidateId = 1L;
        User candidate = createUser(candidateId, "Alice");

        when(userRepository.findById(candidateId)).thenReturn(java.util.Optional.of(candidate));
        when(submissionRepository.countDistinctAssessmentsByCandidate(any(User.class))).thenReturn(3L);
        when(submissionRepository.countByCandidate(any(User.class))).thenReturn(15L);
        when(submissionRepository.countByCandidateAndResult(any(User.class), eq(SubmissionResult.ACCEPTED)))
                .thenReturn(12L);
        when(submissionRepository.countByCandidateAndResult(any(User.class), eq(SubmissionResult.WRONG_ANSWER)))
                .thenReturn(3L);
        when(submissionRepository.findAverageScoreByCandidate(any(User.class))).thenReturn(82.5);
        when(submissionRepository.findMaxScoreByCandidate(any(User.class))).thenReturn(100.0);
        when(submissionRepository.findAverageExecutionTimeByCandidate(any(User.class))).thenReturn(325.5);
        when(codeReviewRepository.findAverageOverallScoreByCandidate(candidateId)).thenReturn(85.0);

        CandidateAnalyticsResponse response = candidateAnalyticsService.getCandidateAnalytics(candidateId);

        assertEquals(candidateId, response.getCandidateId());
        assertEquals(3, response.getAssessmentsAttempted());
        assertEquals(15, response.getTotalSubmissions());
        assertEquals(12, response.getAcceptedCount());
        assertEquals(3, response.getWrongAnswerCount());
        assertEquals(82.5, response.getAverageScore());
        assertEquals(100.0, response.getBestScore());
        assertEquals(80.0, response.getAcceptanceRate()); // 12/15 * 100
        assertEquals(325.5, response.getAverageExecutionTimeMs());
        assertEquals(85.0, response.getAverageAiReviewScore());
        assertEquals("Alice", response.getCandidateName());
        assertEquals("user1@example.com", response.getCandidateEmail());
    }

    private User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail("user" + id + "@example.com");
        user.setRole(Role.CANDIDATE);
        return user;
    }
}
