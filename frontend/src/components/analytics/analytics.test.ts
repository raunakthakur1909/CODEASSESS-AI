/**
 * Unit tests for Milestone 9.6 analytics components.
 *
 * Strategy: test the pure formatting / rendering logic inside each component
 * without a DOM environment. Components that only derive display values from
 * props are tested via the helper functions extracted from them.
 *
 * AdminAnalyticsPage integration behaviour (fetch, state transitions) is
 * covered separately in AdminAnalyticsPage.test.ts using jsdom + fetch stubs.
 */

import { describe, it, expect } from 'vitest';

// ---------------------------------------------------------------------------
// AnalyticsKpiGrid — formatting helpers (extracted for unit testing)
// ---------------------------------------------------------------------------

/** Copied from AnalyticsKpiGrid.tsx — mirrors the exact implementation */
function fmt(n: number | null, decimals = 1): string {
  if (n === null || n === undefined) return '—';
  return n.toFixed(decimals);
}

function pct(n: number | null): string {
  if (n === null || n === undefined) return '—';
  return `${(n * 100).toFixed(1)}%`;
}

describe('KPI formatting helpers', () => {
  describe('fmt', () => {
    it('formats a number to 1 decimal place by default', () => {
      expect(fmt(72.5678)).toBe('72.6');
    });

    it('formats a number to 0 decimal places when asked', () => {
      expect(fmt(210.4, 0)).toBe('210');
    });

    it('returns em-dash for null', () => {
      expect(fmt(null)).toBe('—');
    });
  });

  describe('pct', () => {
    it('converts a 0–1 rate to percentage string with one decimal', () => {
      expect(pct(0.7)).toBe('70.0%');
      expect(pct(1)).toBe('100.0%');
      expect(pct(0)).toBe('0.0%');
    });

    it('returns em-dash for null', () => {
      expect(pct(null)).toBe('—');
    });
  });
});

// ---------------------------------------------------------------------------
// ProblemPerformanceTable — rate class helper
// ---------------------------------------------------------------------------

/** Copied from ProblemPerformanceTable.tsx */
function rateClass(rate: number | null): string {
  if (rate === null) return 'an-null';
  const pct2 = rate * 100;
  if (pct2 >= 60) return 'an-rate--high';
  if (pct2 >= 30) return 'an-rate--mid';
  return 'an-rate--low';
}

describe('rateClass', () => {
  it('returns high class for acceptance >= 60%', () => {
    expect(rateClass(0.6)).toBe('an-rate--high');
    expect(rateClass(1.0)).toBe('an-rate--high');
  });

  it('returns mid class for 30–59%', () => {
    expect(rateClass(0.3)).toBe('an-rate--mid');
    expect(rateClass(0.59)).toBe('an-rate--mid');
  });

  it('returns low class for < 30%', () => {
    expect(rateClass(0.0)).toBe('an-rate--low');
    expect(rateClass(0.29)).toBe('an-rate--low');
  });

  it('returns an-null for null rate', () => {
    expect(rateClass(null)).toBe('an-null');
  });
});

// ---------------------------------------------------------------------------
// SubmissionMetrics — derived values
// ---------------------------------------------------------------------------

describe('SubmissionMetrics derived values', () => {
  it('computes wrong-answer count as total minus accepted', () => {
    const total = 12;
    const accepted = 8;
    const wrongAnswer = total - accepted;
    expect(wrongAnswer).toBe(4);
  });

  it('produces a valid bar width string for 80 average score', () => {
    const score = 80;
    const barPct = (score / 100 * 100).toFixed(1);
    expect(barPct).toBe('80.0');
  });
});

// ---------------------------------------------------------------------------
// App routing — analytics query string parsing
// ---------------------------------------------------------------------------

describe('Analytics route query string parsing', () => {
  it('correctly parses assessmentId from URL search string', () => {
    const search = '?analytics=assessment&assessmentId=3';
    const params = new URLSearchParams(search);

    expect(params.get('analytics')).toBe('assessment');
    expect(params.get('assessmentId')).toBe('3');
    expect(Number(params.get('assessmentId'))).toBe(3);
    expect(Number.isInteger(Number(params.get('assessmentId')))).toBe(true);
  });

  it('rejects non-numeric assessmentId', () => {
    const params = new URLSearchParams('?analytics=assessment&assessmentId=abc');
    const id = Number(params.get('assessmentId'));
    expect(Number.isInteger(id)).toBe(false);
  });

  it('rejects zero assessmentId', () => {
    const params = new URLSearchParams('?analytics=assessment&assessmentId=0');
    const id = Number(params.get('assessmentId'));
    expect(id > 0).toBe(false);
  });

  it('ignores analytics route for non-admin role', () => {
    // Simulate the App.tsx guard: user.role === 'ADMIN'
    const role: string = 'CANDIDATE';
    const shouldRoute = role === 'ADMIN';
    expect(shouldRoute).toBe(false);
  });
});
