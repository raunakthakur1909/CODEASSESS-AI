/**
 * Vitest unit tests for analyticsService — Milestone 9.5
 *
 * Uses vi.stubGlobal('fetch', ...) to mock the global fetch, matching
 * the pattern in useAIReviewPolling.test.ts.
 *
 * localStorage is stubbed via vi.stubGlobal so tests run in the
 * default 'node' environment without needing jsdom.
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { analyticsService } from '../services/analyticsService';
import type {
  AssessmentAnalytics,
  CandidateAnalytics,
  ProblemAnalytics,
  LeaderboardEntry,
  AIAnalytics,
} from '../types/analytics';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function mockResponse<T>(body: T, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    statusText: status === 200 ? 'OK' : 'Error',
    json: () => Promise.resolve(body),
  } as unknown as Response;
}

function stubFetch<T>(body: T, status = 200) {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(mockResponse(body, status)));
}

// ---------------------------------------------------------------------------
// Fixtures
// ---------------------------------------------------------------------------

const ASSESSMENT_ANALYTICS: AssessmentAnalytics = {
  assessmentId: 1,
  assessmentTitle: 'Java Basics',
  totalCandidates: 5,
  totalSubmissions: 12,
  acceptedSubmissions: 8,
  averageScore: 72.5,
  completionRate: 0.8,
  averageExecutionTimeMs: 210.0,
  problems: [
    {
      problemId: 1,
      title: 'Hello World',
      totalSubmissions: 6,
      acceptedSubmissions: 5,
      acceptanceRate: 0.833,
      averageScore: 88.0,
    },
  ],
};

const CANDIDATE_ANALYTICS: CandidateAnalytics = {
  candidateId: 42,
  candidateName: 'Alice',
  candidateEmail: 'alice@example.com',
  assessmentsAttempted: 3,
  totalSubmissions: 10,
  acceptedCount: 7,
  wrongAnswerCount: 2,
  averageScore: 80.0,
  bestScore: 100.0,
  acceptanceRate: 0.7,
  averageExecutionTimeMs: 180.0,
  averageAiReviewScore: null,
};

const PROBLEM_ANALYTICS: ProblemAnalytics = {
  problemId: 7,
  title: 'Two Sum',
  difficulty: 'EASY',
  totalPoints: 100,
  totalAttempts: 20,
  acceptedCount: 14,
  wrongAnswerCount: 4,
  compilationErrorCount: 1,
  runtimeErrorCount: 1,
  acceptanceRate: 0.7,
  averageScore: 78.0,
  averageExecutionTimeMs: 95.0,
  performanceLevel: 'AS_EXPECTED',
};

const LEADERBOARD: LeaderboardEntry[] = [
  {
    rank: 1,
    candidateId: 42,
    candidateName: 'Alice',
    totalScore: 95.0,
    acceptedCount: 3,
    totalProblems: 3,
    averageExecutionTimeMs: 112.0,
    lastSubmissionAt: '2026-08-20T10:00:00',
  },
];

const AI_ANALYTICS: AIAnalytics = {
  totalReviews: 6,
  averageOverallScore: 74.5,
  averageTimeComplexityScore: 68.0,
  averageSpaceComplexityScore: 71.0,
  totalIssuesFound: 18,
  criticalIssues: 1,
  highIssues: 3,
  mediumIssues: 8,
  lowIssues: 6,
  securityIssues: 2,
  performanceIssues: 4,
  maintainabilityIssues: 5,
  complexityIssues: 3,
  bestPracticesIssues: 3,
  bugRiskIssues: 1,
  mostCommonWeaknesses: { PERFORMANCE: 4, MAINTAINABILITY: 5 },
  topImprovementAreas: ['Reduce nested loops', 'Add null checks'],
};

// ---------------------------------------------------------------------------
// localStorage stub — runs cleanly in node environment
// ---------------------------------------------------------------------------

const store: Record<string, string> = {};
const localStorageMock = {
  getItem: vi.fn((key: string) => store[key] ?? null),
  setItem: vi.fn((key: string, value: string) => { store[key] = value; }),
  removeItem: vi.fn((key: string) => { delete store[key]; }),
  clear: vi.fn(() => { Object.keys(store).forEach(k => delete store[k]); }),
};

// ---------------------------------------------------------------------------
// Setup / teardown
// ---------------------------------------------------------------------------

beforeEach(() => {
  vi.stubGlobal('localStorage', localStorageMock);
  store['accessToken'] = 'test.jwt.token';
});

afterEach(() => {
  delete store['accessToken'];
  vi.unstubAllGlobals();
  vi.clearAllMocks();
});

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('analyticsService', () => {

  // ── Assessment ─────────────────────────────────────────────────────────

  describe('getAssessmentAnalytics', () => {
    it('calls the correct endpoint and deserialises AssessmentAnalytics', async () => {
      stubFetch(ASSESSMENT_ANALYTICS);
      const result = await analyticsService.getAssessmentAnalytics(1);

      expect(result).toEqual(ASSESSMENT_ANALYTICS);
      expect(result.problems).toHaveLength(1);
      expect(vi.mocked(fetch).mock.calls[0][0]).toContain('/analytics/assessments/1');
    });

    it('throws with backend message on 403', async () => {
      stubFetch({ message: 'Access denied' }, 403);
      await expect(analyticsService.getAssessmentAnalytics(1)).rejects.toThrow('Access denied');
    });
  });

  // ── Candidate ──────────────────────────────────────────────────────────

  describe('getCandidateAnalytics', () => {
    it('calls /analytics/candidates/{id} and returns CandidateAnalytics', async () => {
      stubFetch(CANDIDATE_ANALYTICS);
      const result = await analyticsService.getCandidateAnalytics(42);

      expect(result.candidateId).toBe(42);
      expect(result.acceptedCount).toBe(7);
      expect(vi.mocked(fetch).mock.calls[0][0]).toContain('/analytics/candidates/42');
    });
  });

  describe('getMyCandidateAnalytics', () => {
    it('calls /analytics/me', async () => {
      stubFetch(CANDIDATE_ANALYTICS);
      await analyticsService.getMyCandidateAnalytics();

      expect(vi.mocked(fetch).mock.calls[0][0]).toContain('/analytics/me');
    });
  });

  // ── Problem ────────────────────────────────────────────────────────────

  describe('getProblemAnalytics', () => {
    it('returns ProblemAnalytics with performanceLevel field', async () => {
      stubFetch(PROBLEM_ANALYTICS);
      const result = await analyticsService.getProblemAnalytics(7);

      expect(result.performanceLevel).toBe('AS_EXPECTED');
      expect(result.acceptanceRate).toBeCloseTo(0.7);
      expect(vi.mocked(fetch).mock.calls[0][0]).toContain('/analytics/problems/7');
    });
  });

  // ── Leaderboard ────────────────────────────────────────────────────────

  describe('getLeaderboard', () => {
    it('calls /leaderboard/assessments/{id} and returns LeaderboardEntry[]', async () => {
      stubFetch(LEADERBOARD);
      const result = await analyticsService.getLeaderboard(1);

      expect(result).toHaveLength(1);
      expect(result[0].rank).toBe(1);
      expect(result[0].candidateName).toBe('Alice');
      expect(vi.mocked(fetch).mock.calls[0][0]).toContain('/leaderboard/assessments/1');
    });
  });

  // ── AI Analytics ───────────────────────────────────────────────────────

  describe('getAIAnalytics', () => {
    it('calls /analytics/ai/assessments/{id} and returns AIAnalytics', async () => {
      stubFetch(AI_ANALYTICS);
      const result = await analyticsService.getAIAnalytics(1);

      expect(result.totalReviews).toBe(6);
      expect(result.mostCommonWeaknesses).toEqual({ PERFORMANCE: 4, MAINTAINABILITY: 5 });
      expect(vi.mocked(fetch).mock.calls[0][0]).toContain('/analytics/ai/assessments/1');
    });
  });

  describe('getMyAIAnalytics', () => {
    it('calls /analytics/ai/me', async () => {
      stubFetch(AI_ANALYTICS);
      await analyticsService.getMyAIAnalytics();

      expect(vi.mocked(fetch).mock.calls[0][0]).toContain('/analytics/ai/me');
    });
  });

  describe('getCandidateAIAnalytics', () => {
    it('calls /analytics/ai/candidates/{id}', async () => {
      stubFetch(AI_ANALYTICS);
      await analyticsService.getCandidateAIAnalytics(42);

      expect(vi.mocked(fetch).mock.calls[0][0]).toContain('/analytics/ai/candidates/42');
    });
  });

  describe('getProblemAIAnalytics', () => {
    it('calls /analytics/ai/problems/{id}', async () => {
      stubFetch(AI_ANALYTICS);
      await analyticsService.getProblemAIAnalytics(7);

      expect(vi.mocked(fetch).mock.calls[0][0]).toContain('/analytics/ai/problems/7');
    });
  });

  // ── Authorization header ───────────────────────────────────────────────

  describe('Authorization header', () => {
    it('sends Bearer token from localStorage', async () => {
      stubFetch(CANDIDATE_ANALYTICS);
      await analyticsService.getMyCandidateAnalytics();

      const callHeaders = vi.mocked(fetch).mock.calls[0][1]?.headers as Headers;
      expect(callHeaders.get('Authorization')).toBe('Bearer test.jwt.token');
    });

    it('omits Authorization header when no token is stored', async () => {
      delete store['accessToken'];
      stubFetch(CANDIDATE_ANALYTICS);
      await analyticsService.getMyCandidateAnalytics();

      const callHeaders = vi.mocked(fetch).mock.calls[0][1]?.headers as Headers;
      expect(callHeaders.get('Authorization')).toBeNull();
    });
  });

  // ── Error handling ─────────────────────────────────────────────────────

  describe('error handling', () => {
    it('uses backend message when available', async () => {
      stubFetch({ message: 'Assessment not found' }, 404);
      await expect(analyticsService.getAssessmentAnalytics(999))
        .rejects.toThrow('Assessment not found');
    });

    it('falls back to generic message when body is not JSON', async () => {
      vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
        json: () => Promise.reject(new SyntaxError('not json')),
      } as unknown as Response));

      await expect(analyticsService.getLeaderboard(1))
        .rejects.toThrow('Unable to load leaderboard');
    });
  });
});
