/**
 * Analytics API service — Milestone 9.5
 *
 * Consistent with the aiReviewService.ts pattern:
 *   - authenticatedRequest() helper for JWT injection
 *   - readError() helper for backend error messages
 *   - all methods return Promise<typed DTO>
 *
 * Methods:
 *   getAssessmentAnalytics(assessmentId)   ADMIN
 *   getCandidateAnalytics(candidateId)     ADMIN | own
 *   getMyCandidateAnalytics()              current user
 *   getProblemAnalytics(problemId)         ADMIN
 *   getLeaderboard(assessmentId)           ADMIN | CANDIDATE
 *   getAIAnalytics(assessmentId)           ADMIN  — assessment AI summary
 *   getCandidateAIAnalytics(candidateId)   ADMIN | own
 *   getMyAIAnalytics()                     current user
 *   getProblemAIAnalytics(problemId)       ADMIN
 */

import type {
  AssessmentAnalytics,
  CandidateAnalytics,
  ProblemAnalytics,
  LeaderboardEntry,
  AIAnalytics,
} from '../types/analytics';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

// ---------------------------------------------------------------------------
// Shared helpers — same structure as aiReviewService.ts
// ---------------------------------------------------------------------------

/**
 * Reads the backend's error body and returns a typed Error.
 * Falls back to the supplied message when the body is not JSON.
 */
async function readError(response: Response, fallbackMessage: string): Promise<Error> {
  try {
    const error = await response.json() as { message?: string };
    return new Error(error.message || fallbackMessage);
  } catch {
    return new Error(fallbackMessage);
  }
}

/**
 * Executes a fetch with the stored JWT in the Authorization header.
 * Mirrors the pattern from aiReviewService.ts exactly.
 */
async function authenticatedRequest(
  path: string,
  options: RequestInit = {},
): Promise<Response> {
  const token = localStorage.getItem('accessToken');
  const headers = new Headers(options.headers);
  headers.set('Accept', 'application/json');

  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  return fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });
}

/**
 * GET a path and deserialise as T.
 * Throws with the backend's error message for non-2xx responses.
 */
async function getJson<T>(path: string, fallbackMessage: string): Promise<T> {
  const response = await authenticatedRequest(path);

  if (!response.ok) {
    throw await readError(response, fallbackMessage);
  }

  return response.json() as Promise<T>;
}

// ---------------------------------------------------------------------------
// Analytics service
// ---------------------------------------------------------------------------

export const analyticsService = {

  // ── Assessment ─────────────────────────────────────────────────────────────

  /**
   * Assessment-wide submission metrics and per-problem breakdown.
   * Requires ADMIN role.
   */
  getAssessmentAnalytics(assessmentId: number): Promise<AssessmentAnalytics> {
    return getJson<AssessmentAnalytics>(
      `/analytics/assessments/${assessmentId}`,
      'Unable to load assessment analytics',
    );
  },

  // ── Candidate ──────────────────────────────────────────────────────────────

  /**
   * Performance analytics for a specific candidate.
   * ADMIN can view any; CANDIDATE can only view their own (403 otherwise).
   */
  getCandidateAnalytics(candidateId: number): Promise<CandidateAnalytics> {
    return getJson<CandidateAnalytics>(
      `/analytics/candidates/${candidateId}`,
      'Unable to load candidate analytics',
    );
  },

  /**
   * Performance analytics for the currently authenticated user.
   * Convenience wrapper — no need to know the caller's own ID.
   */
  getMyCandidateAnalytics(): Promise<CandidateAnalytics> {
    return getJson<CandidateAnalytics>(
      '/analytics/me',
      'Unable to load your analytics',
    );
  },

  // ── Problem ────────────────────────────────────────────────────────────────

  /**
   * Statistics for a specific problem — acceptance rate, difficulty vs actual.
   * Requires ADMIN role.
   */
  getProblemAnalytics(problemId: number): Promise<ProblemAnalytics> {
    return getJson<ProblemAnalytics>(
      `/analytics/problems/${problemId}`,
      'Unable to load problem analytics',
    );
  },

  // ── Leaderboard ────────────────────────────────────────────────────────────

  /**
   * Deterministic ranked leaderboard for an assessment.
   * Ranking: totalScore DESC → acceptedCount DESC → avgTime ASC → lastSubmit ASC
   * Available to ADMIN and CANDIDATE roles.
   */
  getLeaderboard(assessmentId: number): Promise<LeaderboardEntry[]> {
    return getJson<LeaderboardEntry[]>(
      `/leaderboard/assessments/${assessmentId}`,
      'Unable to load leaderboard',
    );
  },

  // ── AI Analytics ───────────────────────────────────────────────────────────

  /**
   * AI review analytics for an entire assessment.
   * Requires ADMIN role.
   */
  getAIAnalytics(assessmentId: number): Promise<AIAnalytics> {
    return getJson<AIAnalytics>(
      `/analytics/ai/assessments/${assessmentId}`,
      'Unable to load AI analytics',
    );
  },

  /**
   * AI review analytics for a specific candidate.
   * ADMIN can view any; CANDIDATE can only view their own.
   */
  getCandidateAIAnalytics(candidateId: number): Promise<AIAnalytics> {
    return getJson<AIAnalytics>(
      `/analytics/ai/candidates/${candidateId}`,
      'Unable to load candidate AI analytics',
    );
  },

  /**
   * AI review analytics for the currently authenticated user.
   */
  getMyAIAnalytics(): Promise<AIAnalytics> {
    return getJson<AIAnalytics>(
      '/analytics/ai/me',
      'Unable to load your AI analytics',
    );
  },

  /**
   * AI review analytics scoped to a specific problem.
   * Requires ADMIN role.
   */
  getProblemAIAnalytics(problemId: number): Promise<AIAnalytics> {
    return getJson<AIAnalytics>(
      `/analytics/ai/problems/${problemId}`,
      'Unable to load problem AI analytics',
    );
  },
};
