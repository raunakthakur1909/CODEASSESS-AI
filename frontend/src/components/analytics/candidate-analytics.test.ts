import { describe, it, expect } from 'vitest';

// ---------------------------------------------------------------------------
// CandidatePerformance — derived values
// ---------------------------------------------------------------------------

describe('CandidatePerformance derived values', () => {
  it('computes acceptanceBarWidth bounded 0-100 directly from acceptanceRate', () => {
    // Unlike admin metrics where it's 0-1, candidate metrics arrive as 0-100
    const computeBarWidth = (rate: number | null) => 
      rate !== null ? Math.min(100, Math.max(0, rate)) : 0;

    expect(computeBarWidth(75.5)).toBe(75.5);
    expect(computeBarWidth(105)).toBe(100); // capped at 100
    expect(computeBarWidth(-5)).toBe(0); // minimum 0
    expect(computeBarWidth(null)).toBe(0);
  });
});

// ---------------------------------------------------------------------------
// AssessmentPerformance — derived values
// ---------------------------------------------------------------------------

describe('AssessmentPerformance derived values', () => {
  it('computes accepted, wrong, and other percentages correctly', () => {
    const total = 20;
    const acceptedCount = 12;
    const wrongAnswerCount = 5;

    const acceptedPct = total > 0 ? (acceptedCount / total) * 100 : 0;
    const wrongPct = total > 0 ? (wrongAnswerCount / total) * 100 : 0;
    
    const otherCount = total - acceptedCount - wrongAnswerCount;
    const otherPct = total > 0 ? (otherCount / total) * 100 : 0;

    expect(acceptedPct).toBe(60);
    expect(wrongPct).toBe(25);
    expect(otherCount).toBe(3);
    expect(otherPct).toBe(15);
  });

  it('handles zero total submissions', () => {
    const total = 0;
    const acceptedCount = 0;
    const wrongAnswerCount = 0;

    const acceptedPct = total > 0 ? (acceptedCount / total) * 100 : 0;
    const wrongPct = total > 0 ? (wrongAnswerCount / total) * 100 : 0;
    const otherCount = total - acceptedCount - wrongAnswerCount;
    const otherPct = total > 0 ? (otherCount / total) * 100 : 0;

    expect(acceptedPct).toBe(0);
    expect(wrongPct).toBe(0);
    expect(otherCount).toBe(0);
    expect(otherPct).toBe(0);
  });
});

// ---------------------------------------------------------------------------
// RecentActivity — derived values
// ---------------------------------------------------------------------------

describe('RecentActivity derived values', () => {
  it('computes submissions per assessment correctly', () => {
    const assessmentsAttempted = 4;
    const totalSubmissions = 22;

    const submissionsPerAssessment =
      assessmentsAttempted > 0
        ? totalSubmissions / assessmentsAttempted
        : null;

    expect(submissionsPerAssessment).toBe(5.5);
  });

  it('handles zero assessments attempted', () => {
    const assessmentsAttempted = 0;
    const totalSubmissions = 0;

    const submissionsPerAssessment =
      assessmentsAttempted > 0
        ? totalSubmissions / assessmentsAttempted
        : null;

    expect(submissionsPerAssessment).toBeNull();
  });
});

// ---------------------------------------------------------------------------
// App routing — Candidate analytics query string parsing
// ---------------------------------------------------------------------------

describe('Candidate analytics route query string parsing', () => {
  it('correctly routes "me" keyword for candidateId', () => {
    const search = '?analytics=candidate&candidateId=me';
    const params = new URLSearchParams(search);

    const type = params.get('analytics');
    const id = params.get('candidateId');

    const shouldRoute = type === 'candidate' && id && (id === 'me' || (Number.isInteger(Number(id)) && Number(id) > 0));

    expect(shouldRoute).toBe(true);
  });

  it('correctly routes numeric candidateId', () => {
    const search = '?analytics=candidate&candidateId=42';
    const params = new URLSearchParams(search);

    const type = params.get('analytics');
    const id = params.get('candidateId');

    const shouldRoute = type === 'candidate' && id && (id === 'me' || (Number.isInteger(Number(id)) && Number(id) > 0));

    expect(shouldRoute).toBe(true);
  });

  it('rejects invalid candidateId (e.g. text)', () => {
    const search = '?analytics=candidate&candidateId=abc';
    const params = new URLSearchParams(search);

    const type = params.get('analytics');
    const id = params.get('candidateId');

    const shouldRoute = type === 'candidate' && id && (id === 'me' || (Number.isInteger(Number(id)) && Number(id) > 0));

    expect(shouldRoute).toBe(false);
  });
});
