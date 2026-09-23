package com.codeassess.backend.analytics.service;

import com.codeassess.backend.analytics.dto.LeaderboardEntry;
import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.submission.SubmissionStatus;
import com.codeassess.backend.model.User;
import com.codeassess.backend.repository.AssessmentRepository;
import com.codeassess.backend.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("LeaderboardService Tests")
class LeaderboardServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    private LeaderboardService leaderboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        leaderboardService = new LeaderboardService(assessmentRepository, submissionRepository);
    }

    @Test
    @DisplayName("Should return empty leaderboard for non-existent assessment")
    void testNonExistentAssessment() {
        Long assessmentId = 999L;
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> leaderboardService.getLeaderboard(assessmentId));
    }

    @Test
    @DisplayName("Should return empty leaderboard when no submissions exist")
    void testEmptySubmissions() {
        Long assessmentId = 1L;
        Assessment assessment = createAssessment(assessmentId);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(submissionRepository.findCompletedSubmissionsByAssessmentForLeaderboard(assessmentId))
                .thenReturn(List.of());

        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(assessmentId);

        assertTrue(leaderboard.isEmpty());
    }

    @Test
    @DisplayName("Should rank by score DESC (higher scores first)")
    void testRankingByScore() {
        Long assessmentId = 1L;
        Assessment assessment = createAssessment(assessmentId);

        User candidate1 = createUser(1L, "Candidate A");
        User candidate2 = createUser(2L, "Candidate B");

        Submission sub1 = createSubmission(1L, candidate1, 80, 100);
        Submission sub2 = createSubmission(2L, candidate2, 90, 100);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(submissionRepository.findCompletedSubmissionsByAssessmentForLeaderboard(assessmentId))
                .thenReturn(List.of(sub1, sub2));

        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(assessmentId);

        assertEquals(2, leaderboard.size());
        assertEquals(1, leaderboard.get(0).getRank());
        assertEquals(90.0, leaderboard.get(0).getTotalScore()); // Candidate B first (90 > 80)
        assertEquals(2, leaderboard.get(1).getRank());
        assertEquals(80.0, leaderboard.get(1).getTotalScore()); // Candidate A second
    }

    @Test
    @DisplayName("Should rank by execution time ASC when scores are tied")
    void testRankingByExecutionTime() {
        Long assessmentId = 1L;
        Assessment assessment = createAssessment(assessmentId);

        User candidate1 = createUser(1L, "Fast Candidate");
        User candidate2 = createUser(2L, "Slow Candidate");

        // Both have score 100
        Submission sub1 = createSubmission(1L, candidate1, 100, 500); // Faster
        Submission sub2 = createSubmission(2L, candidate2, 100, 1000); // Slower

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(submissionRepository.findCompletedSubmissionsByAssessmentForLeaderboard(assessmentId))
                .thenReturn(List.of(sub1, sub2));

        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(assessmentId);

        assertEquals(2, leaderboard.size());
        assertEquals(1, leaderboard.get(0).getRank());
        assertEquals("Fast Candidate", leaderboard.get(0).getCandidateName());
        assertEquals(2, leaderboard.get(1).getRank());
        assertEquals("Slow Candidate", leaderboard.get(1).getCandidateName());
    }

    @Test
    @DisplayName("Should assign sequential ranks starting from 1")
    void testRankAssignment() {
        Long assessmentId = 1L;
        Assessment assessment = createAssessment(assessmentId);

        User user1 = createUser(1L, "User 1");
        User user2 = createUser(2L, "User 2");
        User user3 = createUser(3L, "User 3");

        Submission sub1 = createSubmission(1L, user1, 100, 100);
        Submission sub2 = createSubmission(2L, user2, 90, 100);
        Submission sub3 = createSubmission(3L, user3, 80, 100);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(submissionRepository.findCompletedSubmissionsByAssessmentForLeaderboard(assessmentId))
                .thenReturn(List.of(sub1, sub2, sub3));

        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(assessmentId);

        assertEquals(3, leaderboard.size());
        assertEquals(1, leaderboard.get(0).getRank());
        assertEquals(2, leaderboard.get(1).getRank());
        assertEquals(3, leaderboard.get(2).getRank());
    }

    // Helper methods
    private Assessment createAssessment(Long id) {
        Assessment assessment = new Assessment();
        // Use reflection to set the ID since there's no setter
        try {
            java.lang.reflect.Field idField = Assessment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(assessment, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set Assessment ID", e);
        }
        assessment.setTitle("Test Assessment");
        assessment.setDescription("Test Description");
        assessment.setDurationMinutes(60);
        assessment.setTotalMarks(100);
        return assessment;
    }

    private User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail("user" + id + "@example.com");
        return user;
    }

    private Submission createSubmission(Long id, User candidate, int score, int executionTime) {
        Submission submission = new Submission();
        // Use reflection to set the ID and submittedAt since there's no setter
        try {
            java.lang.reflect.Field idField = Submission.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(submission, id);
            
            java.lang.reflect.Field submittedAtField = Submission.class.getDeclaredField("submittedAt");
            submittedAtField.setAccessible(true);
            submittedAtField.set(submission, java.time.LocalDateTime.now().minusMinutes(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set Submission fields", e);
        }
        submission.setCandidate(candidate);
        submission.setScore(score);
        submission.setExecutionTimeMs(executionTime);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submission.setResult(score == 100 ? SubmissionResult.ACCEPTED : SubmissionResult.WRONG_ANSWER);
        // Set a mock Problem to avoid NullPointerException
        submission.setProblem(createMockProblem(1L));
        return submission;
    }

    private com.codeassess.backend.problem.Problem createMockProblem(Long id) {
        com.codeassess.backend.problem.Problem problem = new com.codeassess.backend.problem.Problem();
        try {
            java.lang.reflect.Field idField = com.codeassess.backend.problem.Problem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(problem, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set Problem ID", e);
        }
        problem.setTitle("Test Problem");
        problem.setDifficulty(com.codeassess.backend.problem.Difficulty.EASY);
        problem.setPoints(100);
        return problem;
    }
}
