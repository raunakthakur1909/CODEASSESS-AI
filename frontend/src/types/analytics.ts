/**
 * Frontend TypeScript types for the Analytics API — Milestone 9.5
 *
 * 1-to-1 mirror of the backend Java DTOs in
 * com.codeassess.backend.analytics.dto
 *
 * Endpoints:
 *   GET /api/analytics/assessments/{id}        → AssessmentAnalytics
 *   GET /api/analytics/candidates/{id}         → CandidateAnalytics
 *   GET /api/analytics/me                      → CandidateAnalytics
 *   GET /api/analytics/problems/{id}           → ProblemAnalytics
 *   GET /api/leaderboard/assessments/{id}      → LeaderboardEntry[]
 *   GET /api/analytics/ai/assessments/{id}     → AIAnalytics
 *   GET /api/analytics/ai/candidates/{id}      → AIAnalytics
 *   GET /api/analytics/ai/me                   → AIAnalytics
 *   GET /api/analytics/ai/problems/{id}        → AIAnalytics
 */

// ---------------------------------------------------------------------------
// Shared / nested
// ---------------------------------------------------------------------------

/**
 * Summary statistics for a single problem within an assessment.
 * Nested inside AssessmentAnalytics.problems[].
 */
export interface ProblemSummary {
  problemId: number;
  title: string;
  totalSubmissions: number;
  acceptedSubmissions: number;
  acceptanceRate: number | null;
  averageScore: number | null;
}

// ---------------------------------------------------------------------------
// AssessmentAnalytics
// ---------------------------------------------------------------------------

/**
 * Aggregate statistics for an entire assessment across all candidates.
 * ADMIN only — GET /api/analytics/assessments/{assessmentId}
 */
export interface AssessmentAnalytics {
  assessmentId: number;
  assessmentTitle: string;
  totalCandidates: number;
  totalSubmissions: number;
  acceptedSubmissions: number;
  averageScore: number | null;
  completionRate: number | null;
  averageExecutionTimeMs: number | null;
  problems: ProblemSummary[];
}

// ---------------------------------------------------------------------------
// CandidateAnalytics
// ---------------------------------------------------------------------------

/**
 * Individual candidate performance across all assessments.
 * ADMIN or own data — GET /api/analytics/candidates/{candidateId}
 *                     GET /api/analytics/me
 *
 * ⚠️  acceptanceRate is a PERCENTAGE (0–100), not a 0–1 ratio.
 *      Backend computes: (acceptedCount / totalSubmissions) * 100
 *      Do NOT multiply by 100 again in the frontend.
 */
export interface CandidateAnalytics {
  candidateId: number;
  candidateName: string;
  candidateEmail: string;
  assessmentsAttempted: number;
  totalSubmissions: number;
  acceptedCount: number;
  wrongAnswerCount: number;
  averageScore: number | null;
  bestScore: number | null;
  /** Percentage value 0–100, e.g. 70.0 means 70% acceptance rate */
  acceptanceRate: number | null;
  averageExecutionTimeMs: number | null;
  averageAiReviewScore: number | null;
}

// ---------------------------------------------------------------------------
// ProblemAnalytics
// ---------------------------------------------------------------------------

/**
 * How hard a problem proved relative to its stated difficulty.
 * Maps to the Java string enum in ProblemAnalyticsResponse.performanceLevel
 */
export type PerformanceLevel =
  | 'EASIER_THAN_EXPECTED'
  | 'AS_EXPECTED'
  | 'HARDER_THAN_EXPECTED';

/** Difficulty enum — matches backend Problem.Difficulty enum values. */
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';

/**
 * Aggregate statistics for a specific problem across all candidates.
 * ADMIN only — GET /api/analytics/problems/{problemId}
 */
export interface ProblemAnalytics {
  problemId: number;
  title: string;
  difficulty: Difficulty | string;
  totalPoints: number;
  totalAttempts: number;
  acceptedCount: number;
  wrongAnswerCount: number;
  compilationErrorCount: number;
  runtimeErrorCount: number;
  acceptanceRate: number | null;
  averageScore: number | null;
  averageExecutionTimeMs: number | null;
  /** EASIER_THAN_EXPECTED | AS_EXPECTED | HARDER_THAN_EXPECTED */
  performanceLevel: PerformanceLevel | string | null;
}

// ---------------------------------------------------------------------------
// LeaderboardEntry
// ---------------------------------------------------------------------------

/**
 * A single ranked entry in an assessment leaderboard.
 * ADMIN + CANDIDATE — GET /api/leaderboard/assessments/{assessmentId}
 *
 * Ranking order (deterministic):
 *  1. totalScore DESC
 *  2. acceptedCount DESC
 *  3. averageExecutionTimeMs ASC  (faster = better)
 *  4. lastSubmissionAt ASC        (earlier = better)
 */
export interface LeaderboardEntry {
  rank: number;
  candidateId: number;
  candidateName: string;
  totalScore: number;
  acceptedCount: number;
  totalProblems: number;
  averageExecutionTimeMs: number | null;
  /** ISO 8601 datetime string from the backend */
  lastSubmissionAt: string | null;
}

// ---------------------------------------------------------------------------
// AIAnalytics
// ---------------------------------------------------------------------------

/** Issue severity levels inside AIAnalytics. */
export type IssueSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

/** Issue category labels matching CodeReviewIssue.category backend enum. */
export type IssueCategory =
  | 'SECURITY'
  | 'PERFORMANCE'
  | 'MAINTAINABILITY'
  | 'COMPLEXITY'
  | 'BEST_PRACTICES'
  | 'BUG_RISK';

/**
 * Aggregate AI code-review analytics.
 *
 * ADMIN or own data:
 *   GET /api/analytics/ai/assessments/{assessmentId}
 *   GET /api/analytics/ai/candidates/{candidateId}
 *   GET /api/analytics/ai/me
 *   GET /api/analytics/ai/problems/{problemId}
 */
export interface AIAnalytics {
  totalReviews: number;
  averageOverallScore: number | null;
  averageTimeComplexityScore: number | null;
  averageSpaceComplexityScore: number | null;
  totalIssuesFound: number;

  // By severity
  criticalIssues: number;
  highIssues: number;
  mediumIssues: number;
  lowIssues: number;

  // By category
  securityIssues: number;
  performanceIssues: number;
  maintainabilityIssues: number;
  complexityIssues: number;
  bestPracticesIssues: number;
  bugRiskIssues: number;

  /** category label → occurrence count */
  mostCommonWeaknesses: Record<string, number> | null;

  /** Human-readable improvement suggestions aggregated from all reviews */
  topImprovementAreas: string[] | null;
}

// ---------------------------------------------------------------------------
// API error shape
// ---------------------------------------------------------------------------

/**
 * Generic error response returned by the backend's GlobalExceptionHandler.
 */
export interface ApiError {
  status: number;
  message: string;
  timestamp?: string;
}
