package com.codeassess.backend.repository;

import com.codeassess.backend.model.User;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.submission.Submission;
import com.codeassess.backend.submission.SubmissionResult;
import com.codeassess.backend.submission.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for managing code submissions.
 * Provides queries for candidate history, problem analytics, and execution queue.
 */
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * Get all submissions by a candidate, newest first
     * Used for candidate dashboard submission history
     */
    List<Submission> findByCandidateOrderBySubmittedAtDesc(User candidate);

    /**
     * Get all submissions for a problem, newest first
     * Used for admin analytics on problem difficulty
     */
    List<Submission> findByProblemOrderBySubmittedAtDesc(Problem problem);

    /**
     * Get all submissions by a candidate for a specific problem, newest first
     * Used to show candidate's attempt history on a problem
     */
    List<Submission> findByCandidateAndProblemOrderBySubmittedAtDesc(
            User candidate,
            Problem problem
    );

    /**
     * Get all submissions with a specific status, ordered by submission time (oldest first)
     * Used for execution queue (e.g., find all QUEUED submissions)
     * FIFO ordering ensures fairness
     */
    List<Submission> findByStatusOrderBySubmittedAtAsc(SubmissionStatus status);

    /**
     * Atomically claim a submission for processing.
     * 
     * This prevents race conditions when multiple workers try to process
     * the same submission. Only one worker will successfully claim it.
     * 
     * Flow:
     * 1. Check if submission is still QUEUED
     * 2. If yes, update to RUNNING (atomic operation)
     * 3. Return number of rows updated (1 = success, 0 = already claimed)
     * 
     * @param id the submission ID
     * @param running the RUNNING status
     * @param queued the QUEUED status
     * @return 1 if successfully claimed, 0 if already claimed by another worker
     */
    @Modifying
    @Query("""
        UPDATE Submission s
        SET s.status = :running
        WHERE s.id = :id
          AND s.status = :queued
    """)
    int claimSubmissionForProcessing(
            @Param("id") Long id,
            @Param("running") SubmissionStatus running,
            @Param("queued") SubmissionStatus queued
    );

    /**
     * Count total submissions by a candidate
     * Used for candidate statistics
     */
    long countByCandidate(User candidate);

    /**
     * Count total submissions for a problem
     * Used for problem analytics
     */
    long countByProblem(Problem problem);

    /**
     * Count submissions by candidate with a specific result
     * Used for candidate statistics (e.g., how many ACCEPTED submissions)
     */
    long countByCandidateAndResult(
            User candidate,
            SubmissionResult result
    );

    // ============================================================================
    // ANALYTICS AGGREGATION QUERIES (Milestone 9.2)
    // ============================================================================

    /**
     * Count submissions by problem and result
     * Used for problem analytics (acceptance rate, error rate)
     */
    long countByProblemAndResult(
            Problem problem,
            SubmissionResult result
    );

    /**
     * Count submissions by problem and status
     * Used for monitoring submission pipeline
     */
    long countByProblemAndStatus(
            Problem problem,
            SubmissionStatus status
    );

    /**
     * Count distinct candidates who submitted for a problem
     * Used for problem analytics (how many candidates attempted)
     */
    @Query("""
        SELECT COUNT(DISTINCT s.candidate.id)
        FROM Submission s
        WHERE s.problem = :problem
    """)
    long countDistinctCandidatesByProblem(@Param("problem") Problem problem);

    /**
     * Count distinct candidates who submitted for an assessment
     * Used for assessment analytics
     */
    @Query("""
        SELECT COUNT(DISTINCT s.candidate.id)
        FROM Submission s
        WHERE s.problem.assessment.id = :assessmentId
    """)
    long countDistinctCandidatesByAssessment(@Param("assessmentId") Long assessmentId);

    /**
     * Calculate average score for a candidate
     * Used for candidate performance metrics
     */
    @Query("""
        SELECT AVG(s.score)
        FROM Submission s
        WHERE s.candidate = :candidate
    """)
    Double findAverageScoreByCandidate(@Param("candidate") User candidate);

    /**
     * Calculate average score for a problem
     * Used for problem difficulty analysis
     */
    @Query("""
        SELECT AVG(s.score)
        FROM Submission s
        WHERE s.problem = :problem
    """)
    Double findAverageScoreByProblem(@Param("problem") Problem problem);

    /**
     * Calculate average score for an assessment
     * Used for assessment-wide analytics
     */
    @Query("""
        SELECT AVG(s.score)
        FROM Submission s
        WHERE s.problem.assessment.id = :assessmentId
    """)
    Double findAverageScoreByAssessment(@Param("assessmentId") Long assessmentId);

    /**
     * Calculate average execution time for a candidate
     * Used for candidate performance metrics
     * NULL execution times are excluded from average
     */
    @Query("""
        SELECT AVG(s.executionTimeMs)
        FROM Submission s
        WHERE s.candidate = :candidate
          AND s.executionTimeMs IS NOT NULL
    """)
    Double findAverageExecutionTimeByCandidate(@Param("candidate") User candidate);

    /**
     * Calculate average execution time for a problem
     * Used for problem performance analysis
     * NULL execution times are excluded from average
     */
    @Query("""
        SELECT AVG(s.executionTimeMs)
        FROM Submission s
        WHERE s.problem = :problem
          AND s.executionTimeMs IS NOT NULL
    """)
    Double findAverageExecutionTimeByProblem(@Param("problem") Problem problem);

    /**
     * Calculate average execution time for an assessment
     * Used for assessment-wide performance metrics
     * NULL execution times are excluded from average
     */
    @Query("""
        SELECT AVG(s.executionTimeMs)
        FROM Submission s
        WHERE s.problem.assessment.id = :assessmentId
          AND s.executionTimeMs IS NOT NULL
    """)
    Double findAverageExecutionTimeByAssessment(@Param("assessmentId") Long assessmentId);

    /**
     * Find maximum score achieved by a candidate
     * Used for candidate best performance tracking
     */
    @Query("""
        SELECT MAX(s.score)
        FROM Submission s
        WHERE s.candidate = :candidate
    """)
    Double findMaxScoreByCandidate(@Param("candidate") User candidate);

    /**
     * Find maximum score achieved for a problem
     * Used to verify if problem is solvable
     */
    @Query("""
        SELECT MAX(s.score)
        FROM Submission s
        WHERE s.problem = :problem
    """)
    Double findMaxScoreByProblem(@Param("problem") Problem problem);

    /**
     * Count total submissions for an assessment
     * Used for assessment analytics
     */
    @Query("""
        SELECT COUNT(s)
        FROM Submission s
        WHERE s.problem.assessment.id = :assessmentId
    """)
    long countByAssessment(@Param("assessmentId") Long assessmentId);

    /**
     * Count submissions by assessment and result
     * Used for assessment-wide acceptance rate
     */
    @Query("""
        SELECT COUNT(s)
        FROM Submission s
        WHERE s.problem.assessment.id = :assessmentId
          AND s.result = :result
    """)
    long countByAssessmentAndResult(
            @Param("assessmentId") Long assessmentId,
            @Param("result") SubmissionResult result
    );

    /**
     * Count distinct assessments attempted by a candidate
     * Used for candidate overview statistics
     */
    @Query("""
        SELECT COUNT(DISTINCT s.problem.assessment.id)
        FROM Submission s
        WHERE s.candidate = :candidate
    """)
    long countDistinctAssessmentsByCandidate(@Param("candidate") User candidate);

    /**
     * Get all submissions for an assessment grouped by candidate for leaderboard
     * Returns list ordered by: score DESC, accepted DESC, execution time ASC, submission time ASC
     * Ranking logic is applied in service layer for flexibility
     */
    @Query("""
        SELECT s
        FROM Submission s
        WHERE s.problem.assessment.id = :assessmentId
          AND s.status = 'COMPLETED'
        ORDER BY s.score DESC, s.submittedAt ASC
    """)
    List<Submission> findCompletedSubmissionsByAssessmentForLeaderboard(
            @Param("assessmentId") Long assessmentId
    );
}
