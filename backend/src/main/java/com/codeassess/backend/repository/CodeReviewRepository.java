package com.codeassess.backend.repository;

import com.codeassess.backend.ai.CodeReview;
import com.codeassess.backend.ai.ReviewStatus;
import com.codeassess.backend.submission.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {

    Optional<CodeReview> findBySubmission(Submission submission);

    boolean existsBySubmission(Submission submission);

    List<CodeReview> findByStatusOrderByCreatedAtDesc(ReviewStatus status);

    List<CodeReview> findByStatusOrderByCreatedAtAsc(ReviewStatus status);

        @Modifying
        @Query("""
            UPDATE CodeReview r
            SET r.status = :processing,
            r.attemptCount = r.attemptCount + 1,
            r.lastError = null
            WHERE r.id = :id
              AND r.status = :pending
              AND (r.nextAttemptAt IS NULL OR r.nextAttemptAt <= :now)
            """)
        int claimPendingReview(
            @Param("id") Long id,
            @Param("pending") ReviewStatus pending,
            @Param("processing") ReviewStatus processing,
            @Param("now") LocalDateTime now
        );

    List<CodeReview> findBySubmissionCandidateIdOrderByCreatedAtDesc(Long candidateId);

    long countByStatus(ReviewStatus status);

    // ============================================================================
    // AI ANALYTICS AGGREGATION QUERIES (Milestone 9.2)
    // ============================================================================

    /**
     * Calculate average overall AI review score for a candidate
     * Used for candidate AI quality metrics
     */
    @Query("""
        SELECT AVG(r.overallScore)
        FROM CodeReview r
        WHERE r.submission.candidate.id = :candidateId
          AND r.status = 'COMPLETED'
          AND r.overallScore IS NOT NULL
    """)
    Double findAverageOverallScoreByCandidate(@Param("candidateId") Long candidateId);

    /**
     * Calculate average overall AI review score for a problem
     * Used for problem quality analysis
     */
    @Query("""
        SELECT AVG(r.overallScore)
        FROM CodeReview r
        WHERE r.submission.problem.id = :problemId
          AND r.status = 'COMPLETED'
          AND r.overallScore IS NOT NULL
    """)
    Double findAverageOverallScoreByProblem(@Param("problemId") Long problemId);

    /**
     * Calculate average overall AI review score for an assessment
     * Used for assessment-wide code quality metrics
     */
    @Query("""
        SELECT AVG(r.overallScore)
        FROM CodeReview r
        WHERE r.submission.problem.assessment.id = :assessmentId
          AND r.status = 'COMPLETED'
          AND r.overallScore IS NOT NULL
    """)
    Double findAverageOverallScoreByAssessment(@Param("assessmentId") Long assessmentId);

    /**
     * Calculate average time complexity score for a candidate
     * Used for candidate algorithmic efficiency metrics
     */
    @Query("""
        SELECT CAST(null AS double)
        FROM CodeReview r
        WHERE r.submission.candidate.id = :candidateId
          AND r.status = 'COMPLETED'
          AND r.complexityTime IS NOT NULL
    """)
    Double findAverageTimeComplexityByCandidate(@Param("candidateId") Long candidateId);

    /**
     * Calculate average time complexity score for a problem
     * Used for problem efficiency analysis
     */
    @Query("""
        SELECT CAST(null AS double)
        FROM CodeReview r
        WHERE r.submission.problem.id = :problemId
          AND r.status = 'COMPLETED'
          AND r.complexityTime IS NOT NULL
    """)
    Double findAverageTimeComplexityByProblem(@Param("problemId") Long problemId);

    /**
     * Calculate average time complexity score for an assessment
     * Used for assessment algorithmic quality metrics
     */
    @Query("""
        SELECT CAST(null AS double)
        FROM CodeReview r
        WHERE r.submission.problem.assessment.id = :assessmentId
          AND r.status = 'COMPLETED'
          AND r.complexityTime IS NOT NULL
    """)
    Double findAverageTimeComplexityByAssessment(@Param("assessmentId") Long assessmentId);

    /**
     * Calculate average space complexity score for a candidate
     * Used for candidate memory efficiency metrics
     */
    @Query("""
        SELECT CAST(null AS double)
        FROM CodeReview r
        WHERE r.submission.candidate.id = :candidateId
          AND r.status = 'COMPLETED'
          AND r.complexitySpace IS NOT NULL
    """)
    Double findAverageSpaceComplexityByCandidate(@Param("candidateId") Long candidateId);

    /**
     * Calculate average space complexity score for a problem
     * Used for problem memory efficiency analysis
     */
    @Query("""
        SELECT CAST(null AS double)
        FROM CodeReview r
        WHERE r.submission.problem.id = :problemId
          AND r.status = 'COMPLETED'
          AND r.complexitySpace IS NOT NULL
    """)
    Double findAverageSpaceComplexityByProblem(@Param("problemId") Long problemId);

    /**
     * Calculate average space complexity score for an assessment
     * Used for assessment memory efficiency metrics
     */
    @Query("""
        SELECT CAST(null AS double)
        FROM CodeReview r
        WHERE r.submission.problem.assessment.id = :assessmentId
          AND r.status = 'COMPLETED'
          AND r.complexitySpace IS NOT NULL
    """)
    Double findAverageSpaceComplexityByAssessment(@Param("assessmentId") Long assessmentId);

    /**
     * Count total completed AI reviews for a candidate
     * Used for candidate AI review statistics
     */
    @Query("""
        SELECT COUNT(r)
        FROM CodeReview r
        WHERE r.submission.candidate.id = :candidateId
          AND r.status = 'COMPLETED'
    """)
    long countCompletedReviewsByCandidate(@Param("candidateId") Long candidateId);

    /**
     * Count total completed AI reviews for a problem
     * Used for problem AI review coverage
     */
    @Query("""
        SELECT COUNT(r)
        FROM CodeReview r
        WHERE r.submission.problem.id = :problemId
          AND r.status = 'COMPLETED'
    """)
    long countCompletedReviewsByProblem(@Param("problemId") Long problemId);

    /**
     * Count total completed AI reviews for an assessment
     * Used for assessment AI review coverage
     */
    @Query("""
        SELECT COUNT(r)
        FROM CodeReview r
        WHERE r.submission.problem.assessment.id = :assessmentId
          AND r.status = 'COMPLETED'
    """)
    long countCompletedReviewsByAssessment(@Param("assessmentId") Long assessmentId);

    @Query(value = """
        SELECT cri.severity, COUNT(*)
        FROM code_review_issues cri
        WHERE cri.review_id IN (
            SELECT cr.id
            FROM code_reviews cr
            JOIN submissions s ON cr.submission_id = s.id
            JOIN problems p ON s.problem_id = p.id
            WHERE p.assessment_id = :assessmentId
              AND cr.status = 'COMPLETED'
        )
        GROUP BY cri.severity
        """, nativeQuery = true)
    List<Object[]> countCompletedIssueSeveritiesByAssessment(@Param("assessmentId") Long assessmentId);

    @Query(value = """
        SELECT cri.category, COUNT(*)
        FROM code_review_issues cri
        WHERE cri.review_id IN (
            SELECT cr.id
            FROM code_reviews cr
            JOIN submissions s ON cr.submission_id = s.id
            JOIN problems p ON s.problem_id = p.id
            WHERE p.assessment_id = :assessmentId
              AND cr.status = 'COMPLETED'
        )
        GROUP BY cri.category
        """, nativeQuery = true)
    List<Object[]> countCompletedIssueCategoriesByAssessment(@Param("assessmentId") Long assessmentId);

    @Query(value = """
        SELECT cri.severity, COUNT(*)
        FROM code_review_issues cri
        WHERE cri.review_id IN (
            SELECT cr.id
            FROM code_reviews cr
            JOIN submissions s ON cr.submission_id = s.id
            WHERE s.candidate_id = :candidateId
              AND cr.status = 'COMPLETED'
        )
        GROUP BY cri.severity
        """, nativeQuery = true)
    List<Object[]> countCompletedIssueSeveritiesByCandidate(@Param("candidateId") Long candidateId);

    @Query(value = """
        SELECT cri.category, COUNT(*)
        FROM code_review_issues cri
        WHERE cri.review_id IN (
            SELECT cr.id
            FROM code_reviews cr
            JOIN submissions s ON cr.submission_id = s.id
            WHERE s.candidate_id = :candidateId
              AND cr.status = 'COMPLETED'
        )
        GROUP BY cri.category
        """, nativeQuery = true)
    List<Object[]> countCompletedIssueCategoriesByCandidate(@Param("candidateId") Long candidateId);

    @Query(value = """
        SELECT cri.severity, COUNT(*)
        FROM code_review_issues cri
        WHERE cri.review_id IN (
            SELECT cr.id
            FROM code_reviews cr
            JOIN submissions s ON cr.submission_id = s.id
            WHERE s.problem_id = :problemId
              AND cr.status = 'COMPLETED'
        )
        GROUP BY cri.severity
        """, nativeQuery = true)
    List<Object[]> countCompletedIssueSeveritiesByProblem(@Param("problemId") Long problemId);

    @Query(value = """
        SELECT cri.category, COUNT(*)
        FROM code_review_issues cri
        WHERE cri.review_id IN (
            SELECT cr.id
            FROM code_reviews cr
            JOIN submissions s ON cr.submission_id = s.id
            WHERE s.problem_id = :problemId
              AND cr.status = 'COMPLETED'
        )
        GROUP BY cri.category
        """, nativeQuery = true)
    List<Object[]> countCompletedIssueCategoriesByProblem(@Param("problemId") Long problemId);
}
